$(document).ready(function() {
    $('body').append(`
        <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 1100">
            <div id="paymentSuccessToast" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="toast-header">
                    <strong class="me-auto">付款成功</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
                <div class="toast-body">
                    訂單已成功付款！
                </div>
            </div>
        </div>
    `);
    const paymentSuccessToastEl = document.getElementById('paymentSuccessToast');
    const paymentSuccessToast = paymentSuccessToastEl ? new bootstrap.Toast(paymentSuccessToastEl) : null;

    AOS.init({
        duration: 800,
        once: true
    });

    const typedOptions = {
        strings: ['探索台北，預訂您的專屬行程', '發現城市角落的美好', '立即規劃您的台北之旅！'],
        typeSpeed: 50,
        backSpeed: 25,
        backDelay: 1500,
        startDelay: 500,
        loop: true,
        showCursor: true,
        cursorChar: '|',
    };
    if ($('#typed-output').length) {
        const typed = new Typed('#typed-output', typedOptions);
    }

    const $tripListContainer = $('#tripListContainer');
    const $backToTopBtn = $('#backToTopBtn');
    const $loginLink = $('#loginLink');
    const $logoutBtn = $('#logoutBtn');
    const $userAvatar = $('#userAvatar');
    const $myBookingsBtn = $('#myBookingsBtn');
    const $districtSelect = $('#districtSelect');
    const $searchInput = $('#searchInput');
    const $searchBtn = $('#searchBtn');

    const loginModalEl = document.getElementById('loginModal');
    const loginModal = loginModalEl ? new bootstrap.Modal(loginModalEl) : null;
    const registerModalEl = document.getElementById('registerModal');
    const registerModal = registerModalEl ? new bootstrap.Modal(registerModalEl) : null;
    const personalInfoModalEl = document.getElementById('personalInfoModal');
    const personalInfoModal = personalInfoModalEl ? new bootstrap.Modal(personalInfoModalEl) : null;
    const bookingSuccessModalEl = document.getElementById('bookingSuccessModal');
    const bookingSuccessModal = bookingSuccessModalEl ? new bootstrap.Modal(bookingSuccessModalEl) : null;
    const myBookingsModalEl = document.getElementById('myBookingsModal');
    const myBookingsModal = myBookingsModalEl ? new bootstrap.Modal(myBookingsModalEl) : null;
    const generalAlertModalEl = document.getElementById('generalAlertModal');
    const generalAlertModal = generalAlertModalEl ? new bootstrap.Modal(generalAlertModalEl) : null;
    const cancelBookingConfirmModalEl = document.getElementById('cancelBookingConfirmModal');
    const cancelBookingConfirmModal = cancelBookingConfirmModalEl ? new bootstrap.Modal(cancelBookingConfirmModalEl) : null;
    const attractionDetailModalEl = document.getElementById('attractionDetailModal');
    const attractionDetailModal = attractionDetailModalEl ? new bootstrap.Modal(attractionDetailModalEl) : null;

    const $generalAlertBody = $('#generalAlertBody');
    const $generalAlertModalLabel = $('#generalAlertModalLabel');
    const $loginErrorPlaceholder = $('#loginErrorPlaceholder');
    const $registerErrorPlaceholder = $('#registerErrorPlaceholder');
    const $bookingErrorPlaceholder = $('#bookingErrorPlaceholder');
    const $myBookingsMsgPlaceholder = $('#myBookingsMsgPlaceholder');
    const $myBookingsListContainer = $('#myBookingsList');
    const $googlePayButtonContainer = $('#googlePayButtonContainer');
    const $submitBookingBtn = $('#submitBookingBtn');
    const $attractionDetailBody = $('#attractionDetailBody');

    let currentBookingData = { tripName: null, bookingDate: null, bookingTime: null, bookingPrice: null, tripImage: null, attractionId: null };
    let currentPendingBookingId = null;
    let userData = null;
    let googlePayClient = null;
    let lastFocusedElement = null;
    let currentApiPage = 0;
    let isLoadingTrips = false;
    let hasMoreTrips = true;

    const API_BASE_URL = 'http://localhost:8080/api';
    const ATTRACTIONS_URL = `${API_BASE_URL}/attractions`;
    const ATTRACTION_DETAIL_URL = (id) => `${API_BASE_URL}/attractions/${id}`;
    const DISTRICTS_URL = `${API_BASE_URL}/districts`;
    const LOGIN_URL = `${API_BASE_URL}/user/login`;
    const REGISTER_URL = `${API_BASE_URL}/user/register`;
    const USER_AUTH_URL = `${API_BASE_URL}/user/auth`;
    const BOOKING_URL = `${API_BASE_URL}/booking`;
    const BOOKINGS_URL = `${API_BASE_URL}/bookings`;
    const BOOKING_PAY_URL = (bookingId) => `${API_BASE_URL}/booking/${bookingId}/pay`;
    const BOOKING_DELETE_URL = (bookingId) => `${BOOKING_URL}/${bookingId}`;


    const GOOGLE_PAY_BASE_CARD_PAYMENT_METHOD = {
        type: 'CARD',
        parameters: {
            allowedAuthMethods: ["PAN_ONLY", "CRYPTOGRAM_3DS"],
            allowedCardNetworks: ["MASTERCARD", "VISA"]
        }
    };
    const GOOGLE_PAY_BASE_CONFIG = {
        apiVersion: 2,
        apiVersionMinor: 0,
        allowedPaymentMethods: [GOOGLE_PAY_BASE_CARD_PAYMENT_METHOD]
    };
    const GOOGLE_PAY_TOKENIZATION_SPECIFICATION = {
        type: 'PAYMENT_GATEWAY',
        parameters: {
            'gateway': 'example',
            'gatewayMerchantId': 'exampleGatewayMerchantId'
        }
    };
    const GOOGLE_PAY_MERCHANT_INFO = {
        merchantName: '台北漫遊'
    };

    function showGeneralAlert(message, title = '提示') {
        if (generalAlertModal && $generalAlertBody.length && $generalAlertModalLabel.length) {
            $generalAlertModalLabel.text(title);
            $generalAlertBody.html(message);
            const activeModalZIndex = Math.max(...$('.modal.show').map(function() { return parseInt($(this).css('z-index')) || 0; }));
            $(generalAlertModalEl).css('z-index', activeModalZIndex + 10);
            generalAlertModal.show();
        } else {
            alert(`${title}: ${message.replace(/<br\s*\/?>/gi, '\n')}`);
        }
    }

    function showSuccessToast(message) {
        if (paymentSuccessToast) {
            $('#paymentSuccessToast .toast-body').html(message);
            paymentSuccessToast.show();
        } else {
            alert(message);
        }
    }

    function showMessage($placeholder, message, isError = false) {
        if (!$placeholder || $placeholder.length === 0) return;
        const alertClass = isError ? 'alert-danger' : 'alert-success';
        $placeholder.html(`<div class="alert ${alertClass} alert-dismissible fade show" role="alert">${message}<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>`).hide().fadeIn(300);
    }

    function clearMessage($placeholder) {
        if ($placeholder && $placeholder.length > 0) {
            $placeholder.fadeOut(200, function() {
                $(this).empty().show();
            });
        }
    }

    function getToken() {
        return localStorage.getItem('authToken');
    }

    function setToken(token) {
        localStorage.setItem('authToken', token);
    }

    function removeToken() {
        localStorage.removeItem('authToken');
    }

    function storeFocus() {
        lastFocusedElement = document.activeElement;
    }

    function restoreFocus() {
        if (lastFocusedElement && typeof lastFocusedElement.focus === 'function') {
            if ($('.modal.show').length === 0) {
                setTimeout(() => {
                    try {
                        lastFocusedElement.focus();
                    } catch (e) { }
                    lastFocusedElement = null;
                }, 0);
            } else {
                lastFocusedElement = null;
            }
        }
    }

    function setupModalFocusManagement(modalElement) {
        if (!modalElement) return;
        $(modalElement).on('shown.bs.modal', function () {
            $(this).find('input, select, button, [href], textarea, [tabindex]:not([tabindex="-1"])').first().trigger('focus');
        });
        $(modalElement).on('show.bs.modal', storeFocus);
        $(modalElement).on('hidden.bs.modal', restoreFocus);
        $(modalElement).on('hide.bs.modal', function(event) {
            if (modalElement.contains(document.activeElement)) {
                if (document.activeElement && typeof document.activeElement.blur === 'function') {
                    try {
                        document.activeElement.blur();
                    } catch (e) { }
                }
            }
        });
    }
    [loginModalEl, registerModalEl, personalInfoModalEl, bookingSuccessModalEl, myBookingsModalEl, generalAlertModalEl, cancelBookingConfirmModalEl, attractionDetailModalEl].forEach(setupModalFocusManagement);

    function updateLoginStateUI(isLoggedIn) {
        if (isLoggedIn) {
            $loginLink.addClass('hidden');
            $userAvatar.removeClass('hidden');
            $logoutBtn.removeClass('hidden');
            $myBookingsBtn.removeClass('hidden');
        } else {
            $loginLink.removeClass('hidden');
            $userAvatar.addClass('hidden');
            $logoutBtn.addClass('hidden');
            $myBookingsBtn.addClass('hidden');
            userData = null;
        }
    }

    function checkLoginStatus() {
        const token = getToken();
        if (!token) {
            updateLoginStateUI(false);
            return Promise.resolve(false);
        }
        return $.ajax({
            url: USER_AUTH_URL,
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` },
            dataType: 'json'
        }).done(function(response) {
            if (response && response.data && response.data.id && response.data.name && response.data.email) {
                userData = response.data;
                updateLoginStateUI(true);
                return true;
            } else {
                removeToken();
                updateLoginStateUI(false);
                return false;
            }
        }).fail(function() {
            removeToken();
            updateLoginStateUI(false);
            return false;
        });
    }

    async function loadDistricts() {
        try {
            const response = await $.ajax({
                url: DISTRICTS_URL,
                method: 'GET',
                dataType: 'json'
            });
            if (response && response.data && Array.isArray(response.data)) {
                $districtSelect.find('option:not(:first)').remove();
                response.data.forEach(district => {
                    $districtSelect.append($('<option>', {
                        value: district,
                        text: district
                    }));
                });
            }
        } catch (error) {
            $districtSelect.prop('disabled', true).append($('<option>', { text: '無法載入區域' }));
        }
    }

    async function loadTrips(page = 0, keyword = '', district = '', append = false) {
        if (isLoadingTrips && append) return;
        isLoadingTrips = true;

        if (!append) {
            $tripListContainer.html('<div class="text-center p-5"><span class="spinner-border text-primary" role="status"><span class="visually-hidden">載入中...</span></span></div>');
            hasMoreTrips = true;
            currentApiPage = 0;
        } else {
             $tripListContainer.append('<div class="text-center p-3 loading-more"><span class="spinner-border spinner-border-sm text-secondary" role="status"><span class="visually-hidden">載入更多...</span></span></div>');
        }

        let url = `${ATTRACTIONS_URL}?page=${page}&size=12`;
        if (keyword) {
            url += `&keyword=${encodeURIComponent(keyword)}`;
        }
        if (district) {
            url += `&district=${encodeURIComponent(district)}`;
        }

        try {
            const response = await $.ajax({
                url: url,
                method: 'GET',
                dataType: 'json'
            });

            $('.loading-more').remove();

            if (response && response.data && Array.isArray(response.data)) {
                renderTripCards(response.data, append);
                currentApiPage = response.nextPage !== null ? response.nextPage : null;
                hasMoreTrips = response.nextPage !== null;
                if (response.data.length === 0 && !append) {
                     $tripListContainer.html('<p class="text-center text-muted p-5">找不到符合條件的行程。</p>');
                }
            } else {
                 if (!append) {
                     $tripListContainer.html('<p class="text-center text-muted p-5">找不到符合條件的行程。</p>');
                 }
                 hasMoreTrips = false;
            }
        } catch (error) {
             $('.loading-more').remove();
             if (!append) {
                 $tripListContainer.html('<p class="text-center text-danger p-5">無法載入行程列表，請稍後再試。</p>');
             }
             hasMoreTrips = false;
        } finally {
            isLoadingTrips = false;
        }
    }

    function renderTripCards(tripsToRender, append = false) {
        if (!append) {
            $tripListContainer.empty();
        }

        if (!tripsToRender || tripsToRender.length === 0) {
            if (!append) {
                 $tripListContainer.html('<p class="text-center text-muted p-5">找不到符合條件的行程。</p>');
            }
            return;
        }

        let cardsHtml = '';
        tripsToRender.forEach((trip, index) => {
            const imageUrl = trip.imageUrl || 'https://placehold.co/280x180/cccccc/969696?text=No+Image';
            const placeholderOnError = `https://placehold.co/280x180/cccccc/969696?text=Image+Error`;
            const timeOptions = `
                <option value="morning" data-price="2000">上半天 (09:00 - 12:00) - NT$2000</option>
                <option value="afternoon" data-price="2500">下半天 (13:00 - 17:00) - NT$2500</option>
            `;
            cardsHtml += `
            <div class="trip-card card" data-aos="fade-up" data-aos-delay="${append ? 0 : index * 50}"
                 data-trip-name="${escapeHtml(trip.name)}"
                 data-trip-image="${escapeHtml(imageUrl)}"
                 data-attraction-id="${trip.id}">
                <img src="${escapeHtml(imageUrl)}" class="card-img-top attraction-detail-trigger" alt="${escapeHtml(trip.name)}" onerror="this.onerror=null;this.src='${placeholderOnError}';">
                <div class="card-body d-flex flex-column">
                    <h5 class="card-title location-name attraction-detail-trigger">${escapeHtml(trip.name)}</h5>
                    <p class="card-text small text-muted mb-2"><i class="fas fa-map-marker-alt me-1"></i>${escapeHtml(trip.district || '未知區域')} | <i class="fas fa-subway me-1"></i>${escapeHtml(trip.mrt || '無捷運資訊')}</p>
                    <div class="mb-2">
                        <label for="date-${trip.id}" class="form-label form-label-sm visually-hidden">日期</label>
                        <input type="date" id="date-${trip.id}" class="form-control form-control-sm date-picker mb-1" required>
                    </div>
                    <div class="mb-3">
                        <label for="time-${trip.id}" class="form-label form-label-sm visually-hidden">時間</label>
                         <select id="time-${trip.id}" class="form-select form-select-sm time-picker" required>
                             ${timeOptions}
                         </select>
                    </div>
                    <button class="btn btn-danger btn-sm book-btn w-100 mt-auto">立即預訂</button>
                </div>
            </div>`;
        });

        $tripListContainer.append(cardsHtml);

        const today = new Date().toISOString().split('T')[0];
        $tripListContainer.find('.date-picker:not([min])').attr('min', today);

        if (!append) {
            AOS.refresh();
        }
    }

    function escapeHtml(unsafe) {
        if (unsafe === null || unsafe === undefined) return '';
        return String(unsafe)
             .replace(/&/g, "&amp;")
             .replace(/</g, "&lt;")
             .replace(/>/g, "&gt;")
             .replace(/"/g, "&quot;")
             .replace(/'/g, "&#039;");
    }

    async function showAttractionDetails(attractionId) {
        if (!attractionDetailModal) return;
        $attractionDetailBody.html('<div class="text-center p-5"><span class="spinner-border text-primary" role="status"><span class="visually-hidden">載入中...</span></span></div>');
        attractionDetailModal.show();

        try {
            const response = await $.ajax({
                url: ATTRACTION_DETAIL_URL(attractionId),
                method: 'GET',
                dataType: 'json'
            });

            if (response && response.data) {
                renderAttractionDetailModal(response.data);
            } else {
                throw new Error('無法解析景點詳細資料');
            }
        } catch (error) {
            let errorMsg = '無法載入景點詳細資訊，請稍後再試。';
             if (error.responseJSON && error.responseJSON.message) {
                 errorMsg = error.responseJSON.message;
             } else if (error.status === 404) {
                 errorMsg = '找不到指定的景點資訊。';
             }
            $attractionDetailBody.html(`<p class="text-center text-danger p-5">${errorMsg}</p>`);
        }
    }

    function renderAttractionDetailModal(detail) {
        let imagesHtml = '';
        const placeholderOnError = `https://placehold.co/800x300/cccccc/969696?text=Image+Error`;
        const noImageHtml = `<div class="single-image-container"><img src="https://placehold.co/800x300/cccccc/969696?text=No+Image" alt="${escapeHtml(detail.name)}"></div>`;
        let numImages = 0;

        if (detail.images && Array.isArray(detail.images) && detail.images.length > 0) {
            detail.images.forEach(imgUrl => {
                let resolvedImgUrl = escapeHtml(imgUrl);
                if (!resolvedImgUrl.toLowerCase().startsWith('http')) {
                    resolvedImgUrl = (API_BASE_URL.startsWith('http') ? '' : window.location.origin) + (resolvedImgUrl.startsWith('/') ? resolvedImgUrl : '/' + resolvedImgUrl);
                }
                imagesHtml += `<div><img src="${resolvedImgUrl}" alt="${escapeHtml(detail.name)}" onerror="this.onerror=null;this.src='${placeholderOnError}';"></div>`;
            });
            numImages = detail.images.length;
        } else if (detail.imageUrl) {
             let resolvedImgUrl = escapeHtml(detail.imageUrl);
             if (!resolvedImgUrl.toLowerCase().startsWith('http')) {
                 resolvedImgUrl = (API_BASE_URL.startsWith('http') ? '' : window.location.origin) + (resolvedImgUrl.startsWith('/') ? resolvedImgUrl : './' + resolvedImgUrl);
             }
             imagesHtml += `<div class="single-image-container"><img src="${resolvedImgUrl}" alt="${escapeHtml(detail.name)}" onerror="this.onerror=null;this.src='${placeholderOnError}';"></div>`;
             numImages = 1;
        } else {
            imagesHtml = noImageHtml;
            numImages = 1;
        }
        if (!imagesHtml) {
            imagesHtml = noImageHtml;
            numImages = 1;
        }

        const detailHtml = `
            <h5 class="mb-3">${escapeHtml(detail.name)}</h5>
            <div class="attraction-images-carousel mb-4">
                ${imagesHtml}
            </div>
            <h6><i class="fas fa-info-circle me-2 text-primary"></i>景點介紹</h6>
            <p>${escapeHtml(detail.description || '無詳細介紹')}</p>
            <h6><i class="fas fa-map-marked-alt me-2 text-primary"></i>地址與區域</h6>
            <p><strong>行政區:</strong> ${escapeHtml(detail.district || '未分類')}</p>
            <p><strong>地址:</strong> ${escapeHtml(detail.address || '無地址資訊')}</p>
            <h6><i class="fas fa-subway me-2 text-primary"></i>交通資訊</h6>
            <p><strong>捷運站:</strong> ${escapeHtml(detail.mrt || '無')}</p>
            <p><strong>交通方式:</strong> ${escapeHtml(detail.transport || '無詳細交通方式')}</p>
            <h6><i class="fas fa-tags me-2 text-primary"></i>分類</h6>
            <p>${escapeHtml(detail.category || '未分類')}</p>
        `;
        $attractionDetailBody.html(detailHtml);

        const $carousel = $attractionDetailBody.find('.attraction-images-carousel');
        if ($carousel.length && $.fn.slick) {
            if ($carousel.hasClass('slick-initialized')) {
                $carousel.slick('unslick');
            }
            if (numImages > 1) {
                $carousel.slick({
                    dots: true,
                    infinite: true,
                    speed: 500,
                    fade: true,
                    cssEase: 'linear',
                    autoplay: true,
                    autoplaySpeed: 3000,
                    arrows: true
                });
            } else {
            }
        }
    }


    function initializeGooglePay() {
        try {
            if (window.google && window.google.payments && window.google.payments.api) {
                googlePayClient = new google.payments.api.PaymentsClient({ environment: 'TEST' });
                checkGooglePayReady();
            }
        } catch (error) {
        }
    }

    function checkGooglePayReady() {
        if (!googlePayClient) {
            return Promise.resolve(false);
        }
        const isReadyToPayRequest = Object.assign({}, GOOGLE_PAY_BASE_CONFIG);
        return googlePayClient.isReadyToPay(isReadyToPayRequest)
            .then(response => {
                return response.result;
            })
            .catch(err => {
                return false;
            });
    }

    function addGooglePayButton(price, currency = 'TWD', $container = $googlePayButtonContainer) {
        if (!googlePayClient) {
            $container.html('<p class="text-danger small">無法載入 Google Pay 按鈕。</p>').removeClass('hidden');
            return;
        }

        checkGooglePayReady().then(isReady => {
            if (isReady) {
                const button = googlePayClient.createButton({
                    onClick: () => onGooglePayButtonClicked(price, currency),
                    allowedPaymentMethods: [GOOGLE_PAY_BASE_CARD_PAYMENT_METHOD]
                });
                $container.empty().append(button).removeClass('hidden');
            } else {
                $container.html('<p class="text-muted small">此裝置不支援 Google Pay。</p>').removeClass('hidden');
            }
        });
    }

    function onGooglePayButtonClicked(price, currency) {
        if (!googlePayClient) {
            showGeneralAlert("Google Pay 初始化失敗，無法進行支付。", "錯誤");
            return;
        }
        if (currentPendingBookingId === null) {
            showGeneralAlert("沒有有效的預訂可供支付。", "錯誤");
            return;
        }

        const paymentDataRequest = Object.assign({}, GOOGLE_PAY_BASE_CONFIG);
        paymentDataRequest.allowedPaymentMethods[0].tokenizationSpecification = GOOGLE_PAY_TOKENIZATION_SPECIFICATION;
        paymentDataRequest.transactionInfo = {
            totalPriceStatus: 'FINAL',
            totalPrice: String(price),
            currencyCode: currency,
            countryCode: 'TW'
        };
        paymentDataRequest.merchantInfo = GOOGLE_PAY_MERCHANT_INFO;

        googlePayClient.loadPaymentData(paymentDataRequest)
            .then(paymentData => {
                const paymentToken = paymentData.paymentMethodData.tokenizationData.token;
                processPaymentOnServer(currentPendingBookingId, paymentToken);

            })
            .catch(err => {
                let errorMsg = "Google Pay 支付失敗。";
                if (err.statusCode === 'CANCELED') {
                    errorMsg = "您已取消 Google Pay 支付。";
                } else if (err.statusCode) {
                    errorMsg = `Google Pay 錯誤 (${err.statusCode})，請稍後再試。`;
                }
                if (personalInfoModal && $(personalInfoModalEl).hasClass('show')) {
                    showMessage($bookingErrorPlaceholder, errorMsg, true);
                } else if (myBookingsModal && $(myBookingsModalEl).hasClass('show')) {
                    showMessage($myBookingsMsgPlaceholder, errorMsg, true);
                    const $bookingItem = $myBookingsListContainer.find(`.booking-item[data-booking-id="${currentPendingBookingId}"]`);
                    $bookingItem.find('.gpay-placeholder').empty().addClass('hidden');
                    $bookingItem.find('.pay-booking-btn, .delete-booking-btn').removeClass('hidden');
                } else {
                     showGeneralAlert(errorMsg, "支付錯誤");
                }
                currentPendingBookingId = null;
            });
    }

    function processPaymentOnServer(bookingId, paymentNonce) {
        const token = getToken();
        if (!token) {
             showGeneralAlert('您的登入已逾時，無法完成付款，請重新登入。', '錯誤');
             if (personalInfoModal && $(personalInfoModalEl).hasClass('show')) { personalInfoModal.hide(); }
             if (myBookingsModal && $(myBookingsModalEl).hasClass('show')) { myBookingsModal.hide(); }
             if (loginModal) { clearMessage($loginErrorPlaceholder); showMessage($loginErrorPlaceholder, '請重新登入', true); loginModal.show(); }
             return;
        }

        const payload = {
            prime: paymentNonce,
            order: {
                booking_id: bookingId,
                price: currentBookingData.bookingPrice,
                trip: {
                    attraction: {
                        id: currentBookingData.attractionId,
                        name: currentBookingData.tripName,
                        address: "",
                        image: currentBookingData.tripImage
                    },
                    date: currentBookingData.bookingDate,
                    time: currentBookingData.bookingTime
                },
                contact: {
                    name: $('#name').val(),
                    phone: $('#phone').val(),
                    email: userData ? userData.email : ""
                }
            }
        };

        $.ajax({
            url: BOOKING_PAY_URL(bookingId),
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            contentType: 'application/json',
            data: JSON.stringify(payload),
            dataType: 'json',
            beforeSend: function() {
                if (personalInfoModal && $(personalInfoModalEl).hasClass('show')) {
                    showMessage($bookingErrorPlaceholder, '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 處理付款中...', false);
                } else if (myBookingsModal && $(myBookingsModalEl).hasClass('show')) {
                     showMessage($myBookingsMsgPlaceholder, '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 處理付款中...', false);
                }
            },
            success: function(response) {
                const isPaymentFromMyBookings = myBookingsModal && $(myBookingsModalEl).hasClass('show');
                if (personalInfoModal && $(personalInfoModalEl).hasClass('show')) {
                    personalInfoModal.hide();
                    if (bookingSuccessModal) bookingSuccessModal.show();
                } else if (isPaymentFromMyBookings) {
                    $myBookingsBtn.trigger('click');
                    showSuccessToast(`訂單 ${bookingId} 付款成功！`);
                }
                currentPendingBookingId = null;
            },
            error: function(jqXHR) {
                let errorMsg = "後端處理支付失敗，請聯繫客服。";
                if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
                    errorMsg = jqXHR.responseJSON.message;
                } else if (jqXHR.status === 400) {
                    errorMsg = "付款請求無效或訂單狀態錯誤。";
                } else if (jqXHR.status === 401 || jqXHR.status === 403) {
                     errorMsg = '您的登入已逾時或無權限，請重新登入。';
                     removeToken(); updateLoginStateUI(false);
                } else if (jqXHR.status === 404) {
                     errorMsg = '找不到指定的預訂。';
                 } else if (jqXHR.status === 0) {
                     errorMsg = '無法連線至伺服器，請檢查網路連線。';
                 } else {
                     errorMsg = `處理付款時發生錯誤 (${jqXHR.status})。`;
                 }

                if (personalInfoModal && $(personalInfoModalEl).hasClass('show')) {
                    showMessage($bookingErrorPlaceholder, errorMsg, true);
                     $('#bookingForm').removeClass('hidden');
                     $submitBookingBtn.removeClass('hidden').prop('disabled', false).text('建立訂單以進行付款');
                     $googlePayButtonContainer.addClass('hidden').empty();
                } else if (myBookingsModal && $(myBookingsModalEl).hasClass('show')) {
                    showMessage($myBookingsMsgPlaceholder, errorMsg, true);
                    const $bookingItem = $myBookingsListContainer.find(`.booking-item[data-booking-id="${bookingId}"]`);
                    $bookingItem.find('.gpay-placeholder').empty().addClass('hidden');
                    $bookingItem.find('.pay-booking-btn, .delete-booking-btn').removeClass('hidden');
                } else {
                     showGeneralAlert(errorMsg, "支付錯誤");
                }
                currentPendingBookingId = null;
            }
        });
    }


    $loginLink.on('click', function() {
        storeFocus();
        clearMessage($loginErrorPlaceholder);
        $('#loginForm')[0].reset();
        if (loginModal) loginModal.show();
    });

    $('#showRegisterBtn').on('click', function() {
        if (loginModal) loginModal.hide();
        clearMessage($registerErrorPlaceholder);
        $('#registerForm')[0].reset();
        if (registerModal) registerModal.show();
    });

    $('#showLoginBtn').on('click', function() {
        if (registerModal) registerModal.hide();
        clearMessage($loginErrorPlaceholder);
        $('#loginForm')[0].reset();
        if (loginModal) loginModal.show();
    });

    $('#loginForm').on('submit', function(event) {
        event.preventDefault();
        const email = $('#username').val().trim();
        const password = $('#password').val().trim();

        if (!email || !password) {
            showMessage($loginErrorPlaceholder, '請輸入電子郵件和密碼！', true);
            return;
        }
        clearMessage($loginErrorPlaceholder);

        const loginPayload = { email, password };
        const $loginSubmitBtn = $('#loginSubmitBtn');

        $.ajax({
            url: LOGIN_URL,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(loginPayload),
            dataType: 'json',
            beforeSend: function() {
                $loginSubmitBtn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 登入中...');
            },
            success: function(response) {
                let tokenToUse = null;
                if (response && response.data && response.data.token) {
                    tokenToUse = response.data.token;
                } else if (response && response.token) {
                    tokenToUse = response.token;
                }

                if (tokenToUse) {
                    setToken(tokenToUse);
                    if (loginModal) loginModal.hide();
                    $('#loginForm')[0].reset();
                    checkLoginStatus();
                } else {
                    showMessage($loginErrorPlaceholder, '登入成功但發生未預期的錯誤 (缺少 Token)。', true);
                    removeToken();
                    updateLoginStateUI(false);
                }
            },
            error: function(jqXHR) {
                let errorMsg = '登入失敗。';
                if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
                    errorMsg = jqXHR.responseJSON.message;
                } else if (jqXHR.status === 400 || jqXHR.status === 401) {
                    errorMsg = '電子郵件或密碼錯誤。';
                } else if (jqXHR.status === 0) {
                     errorMsg = '無法連線至伺服器，請檢查網路連線。';
                 } else {
                    errorMsg = `登入失敗 (${jqXHR.status})，請稍後再試。`;
                }
                showMessage($loginErrorPlaceholder, errorMsg, true);
                removeToken();
                updateLoginStateUI(false);
            },
            complete: function() {
                $loginSubmitBtn.prop('disabled', false).text('登入');
            }
        });
    });

    $('#registerForm').on('submit', function(event) {
        event.preventDefault();
        const name = $('#registerName').val().trim();
        const email = $('#registerEmail').val().trim();
        const password = $('#registerPassword').val().trim();
        const age = $('#registerAge').val() ? parseInt($('#registerAge').val()) : null;
        const gender = $('#registerGender').val() || null;

        if (!name || !email || !password) {
            showMessage($registerErrorPlaceholder, '姓名、Email和密碼皆為必填！', true);
            return;
        }
        if (password.length < 6) {
            showMessage($registerErrorPlaceholder, '密碼長度至少需要6位！', true);
            return;
        }
        if (!/\S+@\S+\.\S+/.test(email)) {
             showMessage($registerErrorPlaceholder, '請輸入有效的電子郵件格式！', true);
             return;
         }
        clearMessage($registerErrorPlaceholder);

        const registerPayload = { name, email, password, age, gender };
        const $registerSubmitBtn = $('#registerSubmitBtn');

        $.ajax({
            url: REGISTER_URL,
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(registerPayload),
            dataType: 'json',
            beforeSend: function() {
                $registerSubmitBtn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 註冊中...');
            },
            success: function(response) {
                if (response && response.ok) {
                    if (registerModal) registerModal.hide();
                    $('#registerForm')[0].reset();
                    clearMessage($loginErrorPlaceholder);
                    $('#loginForm')[0].reset();
                    showMessage($loginErrorPlaceholder, '註冊成功！現在您可以登入了。', false);
                    if (loginModal) loginModal.show();
                } else {
                    const errorMsg = (response && response.message) ? response.message : '註冊失敗，請稍後再試。';
                    showMessage($registerErrorPlaceholder, errorMsg, true);
                }
            },
            error: function(jqXHR) {
                let errorMsg = '註冊失敗。';
                if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
                    errorMsg = jqXHR.responseJSON.message;
                } else if (jqXHR.status === 400) {
                    errorMsg = '註冊失敗，此 Email 可能已被註冊或輸入資料有誤。';
                } else if (jqXHR.status === 0) {
                     errorMsg = '無法連線至伺服器，請檢查網路連線。';
                 } else {
                    errorMsg = `註冊失敗 (${jqXHR.status})，請稍後再試。`;
                }
                showMessage($registerErrorPlaceholder, errorMsg, true);
            },
            complete: function() {
                $registerSubmitBtn.prop('disabled', false).text('完成註冊');
            }
        });
    });

    $logoutBtn.on('click', function() {
        removeToken();
        updateLoginStateUI(false);
    });

    $tripListContainer.on('click', '.book-btn', function() {
        storeFocus();
        const token = getToken();
        if (!token) {
            clearMessage($loginErrorPlaceholder);
            showMessage($loginErrorPlaceholder, '請先登入會員才能預訂行程！', true);
            if (loginModal) loginModal.show();
            return;
        }

        const $card = $(this).closest('.trip-card');
        const tripName = $card.data('trip-name');
        const tripImage = $card.data('trip-image');
        const attractionId = $card.data('attraction-id');
        const $bookingDateInput = $card.find('.date-picker');
        const bookingDate = $bookingDateInput.val();
        const $timeSelect = $card.find('.time-picker');
        const $selectedTimeOption = $timeSelect.find('option:selected');
        const bookingTime = $selectedTimeOption.val();
        const bookingPrice = $selectedTimeOption.data('price');

        if (!bookingDate) {
            showGeneralAlert('請先選擇預訂日期！', '錯誤');
            $bookingDateInput.focus();
            return;
        }
        if (!bookingTime) {
            showGeneralAlert('請選擇預訂時間！', '錯誤');
            $timeSelect.focus();
            return;
        }
        if (!attractionId) {
            showGeneralAlert("無法預訂此行程，缺少景點 ID。", "錯誤");
            return;
        }
        if (bookingPrice === undefined || bookingPrice === null || isNaN(bookingPrice)) {
            showGeneralAlert("無法預訂此行程，缺少或價格資訊無效。", "錯誤");
            return;
        }

        currentBookingData = { tripName, bookingDate, bookingTime, bookingPrice, tripImage, attractionId };

        $('#confirmTripName').text(tripName);
        $('#confirmTripImage').attr('src', tripImage || 'https://placehold.co/100x75/E8E8E8/BDBDBD?text=Trip');
        $('#confirmBookingDate').text(bookingDate);
        $('#confirmBookingTime').text(bookingTime === 'morning' ? '上半天 (09:00 - 12:00)' : '下半天 (13:00 - 17:00)');
        $('#confirmBookingPrice').text(bookingPrice);
        $('#currentBookingId').val('');
        clearMessage($bookingErrorPlaceholder);
        $('#bookingForm')[0].reset();

        if (userData && userData.name) {
            $('#name').val(userData.name);
        } else {
             if (getToken()) {
                 checkLoginStatus().then(() => {
                     if (userData && userData.name) $('#name').val(userData.name);
                 });
             }
        }
        $('#customerIdNumber').val('').removeClass('is-invalid');
        $('#phone').val('').removeClass('is-invalid');
        $('#name').removeClass('is-invalid');
        $('#bookingForm').removeClass('hidden');
        $submitBookingBtn.removeClass('hidden').prop('disabled', false).text('建立訂單以進行付款');
        $googlePayButtonContainer.addClass('hidden').empty();

        if (personalInfoModal) personalInfoModal.show();
    });

    $('#bookingForm').on('submit', function(event) {
        event.preventDefault();
        const token = getToken();
        if (!token) {
            showMessage($bookingErrorPlaceholder, '您的登入已逾時，請重新登入。', true);
            if (personalInfoModal) personalInfoModal.hide();
            if (loginModal) {
                clearMessage($loginErrorPlaceholder);
                showMessage($loginErrorPlaceholder, '您的登入已逾時，請重新登入後再預訂。', true);
                loginModal.show();
            }
            return;
        }

        const name = $('#name').val().trim();
        const customerIdNumber = $('#customerIdNumber').val().trim().toUpperCase();
        const phone = $('#phone').val().trim();
        let isValid = true;
        let errorMessages = [];
        const idRegex = /^[A-Z][12]\d{8}$/;
        const phoneRegex = /^09\d{8}$/;

        $('.is-invalid').removeClass('is-invalid');
        if (!name) {
            errorMessages.push('請填寫聯絡姓名！');
            isValid = false;
            $('#name').addClass('is-invalid');
        }
        if (!customerIdNumber) {
            errorMessages.push('請填寫身分證字號！');
            isValid = false;
            $('#customerIdNumber').addClass('is-invalid');
        } else if (!idRegex.test(customerIdNumber)) {
            errorMessages.push('身分證字號格式不正確 (應為1位大寫英文字母 + 9位數字)。');
            isValid = false;
            $('#customerIdNumber').addClass('is-invalid');
        }
        if (!phone) {
            errorMessages.push('請填寫聯絡電話！');
            isValid = false;
            $('#phone').addClass('is-invalid');
        } else if (!phoneRegex.test(phone)) {
            errorMessages.push('聯絡電話格式不正確 (應為 09 開頭的 10 位數字)。');
            isValid = false;
            $('#phone').addClass('is-invalid');
        }

        if (!isValid) {
            showMessage($bookingErrorPlaceholder, errorMessages.join('<br>'), true);
            return;
        } else {
            clearMessage($bookingErrorPlaceholder);
        }

        if (!currentBookingData.attractionId || !currentBookingData.bookingDate || !currentBookingData.bookingTime || currentBookingData.bookingPrice === null) {
            showMessage($bookingErrorPlaceholder, '預訂資訊不完整或已遺失，請關閉視窗後重新選擇行程。', true);
            return;
        }

        const bookingPayload = {
            attractionId: currentBookingData.attractionId,
            date: currentBookingData.bookingDate,
            time: currentBookingData.bookingTime,
            price: currentBookingData.bookingPrice,
            contactName: name,
            customerIdNumber: customerIdNumber,
            contactPhone: phone
        };

        $.ajax({
            url: BOOKING_URL,
            method: 'POST',
            contentType: 'application/json',
            headers: { 'Authorization': `Bearer ${token}` },
            data: JSON.stringify(bookingPayload),
            dataType: 'json',
            beforeSend: function() {
                $submitBookingBtn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 建立訂單中...');
            },
            success: function(response) {
                if (response && response.data && response.data.id) {
                    currentPendingBookingId = response.data.id;
                    $('#currentBookingId').val(currentPendingBookingId);
                    $('#bookingForm').addClass('hidden');
                    $submitBookingBtn.addClass('hidden');
                    addGooglePayButton(currentBookingData.bookingPrice);
                } else {
                    const errorMsg = (response && response.message) ? response.message : '建立訂單失敗，後端回應異常。';
                    showMessage($bookingErrorPlaceholder, errorMsg, true);
                    $submitBookingBtn.prop('disabled', false).text('建立訂單以進行付款');
                }
            },
            error: function(jqXHR) {
                let errorMsg = '建立訂單失敗。';
                if (jqXHR.responseJSON) {
                    if (jqXHR.responseJSON.message) {
                        errorMsg = jqXHR.responseJSON.message;
                    }
                    if (jqXHR.responseJSON.errors && typeof jqXHR.responseJSON.errors === 'object') {
                        const fieldErrors = Object.entries(jqXHR.responseJSON.errors)
                                                 .map(([field, msg]) => `${msg}`)
                                                 .join('<br>');
                        if (fieldErrors) {
                            errorMsg = `資料驗證失敗：<br>${fieldErrors}`;
                        }
                    }
                } else if (jqXHR.status === 401 || jqXHR.status === 403) {
                    errorMsg = '您的登入已逾時或無權限，請重新登入。';
                    removeToken(); updateLoginStateUI(false);
                    if (personalInfoModal) personalInfoModal.hide();
                    if (loginModal) { clearMessage($loginErrorPlaceholder); showMessage($loginErrorPlaceholder, errorMsg, true); loginModal.show(); }
                } else if (jqXHR.status === 400) {
                    errorMsg = '提交的資料有誤、不完整或已有相同預訂。';
                } else if (jqXHR.status === 404) {
                     errorMsg = '找不到指定的景點，無法建立預訂。';
                 } else if (jqXHR.status === 500) {
                    errorMsg = '伺服器內部錯誤，無法建立預訂。';
                } else if (jqXHR.status === 0) {
                     errorMsg = '無法連線至伺服器，請檢查網路連線。';
                 } else {
                    errorMsg = `建立訂單失敗 (${jqXHR.status})。`;
                }
                showMessage($bookingErrorPlaceholder, errorMsg, true);
                $submitBookingBtn.prop('disabled', false).text('建立訂單以進行付款');
            }
        });
    });

    $myBookingsBtn.on('click', function() {
         storeFocus();
         const token = getToken();
        if (!token) {
            if (loginModal) {
                clearMessage($loginErrorPlaceholder);
                showMessage($loginErrorPlaceholder, '請先登入以查看您的預訂紀錄。', true);
                loginModal.show();
            }
            return;
        }
        if (!myBookingsModal) return;

        $myBookingsListContainer.empty();
        clearMessage($myBookingsMsgPlaceholder);
        showMessage($myBookingsMsgPlaceholder, '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 載入預訂資料中...', false);
        myBookingsModal.show();

        $.ajax({
            url: BOOKINGS_URL,
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` },
            dataType: 'json',
            success: function(response) {
                clearMessage($myBookingsMsgPlaceholder);
                const bookings = (response && response.data && Array.isArray(response.data)) ? response.data : [];
                if (bookings.length > 0) {
                    renderMyBookingsList(bookings);
                } else {
                    showMessage($myBookingsMsgPlaceholder, '您目前沒有任何預訂紀錄。', false);
                }
            },
            error: function(jqXHR) {
                clearMessage($myBookingsMsgPlaceholder);
                let errorMsg = '查詢預訂紀錄時發生錯誤。';
                 if (jqXHR.status === 401 || jqXHR.status === 403) {
                    errorMsg = '您的登入已逾時或無權限，請重新登入。';
                    removeToken(); updateLoginStateUI(false);
                    myBookingsModal.hide();
                    if (loginModal) { clearMessage($loginErrorPlaceholder); showMessage($loginErrorPlaceholder, errorMsg, true); loginModal.show(); }
                } else if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
                    errorMsg = jqXHR.responseJSON.message;
                } else if (jqXHR.status === 0) {
                     errorMsg = '無法連線至伺服器，請檢查網路連線。';
                 } else {
                    errorMsg = `查詢預訂紀錄失敗 (${jqXHR.status})。`;
                }
                showMessage($myBookingsMsgPlaceholder, errorMsg, true);
            }
        });
    });

    function renderMyBookingsList(bookings) {
        $myBookingsListContainer.empty();
        if (!bookings || bookings.length === 0) {
            showMessage($myBookingsMsgPlaceholder, '您目前沒有任何預訂紀錄。', false);
            return;
        }
        try {
            bookings.sort((a, b) => new Date(b.date) - new Date(a.date));
        } catch (e) { }

        bookings.forEach(booking => {
            const attractionName = booking.attraction?.name || '景點名稱未知';
            const imageUrl = booking.attraction?.image || 'https://placehold.co/80x60/E8E8E8/BDBDBD?text=No+Image';
            let dateStr = '日期未知';
            try {
                if (booking.date) {
                    dateStr = new Date(booking.date).toLocaleDateString('zh-TW', { year: 'numeric', month: 'long', day: 'numeric' });
                }
            } catch (e) { }
            const timeStr = booking.time === 'morning' ? '上半天 (09:00-12:00)' : (booking.time === 'afternoon' ? '下半天 (13:00-17:00)' : '時間未知');
            const price = booking.price;
            const priceStr = (price !== null && !isNaN(price)) ? `NT$ ${Number(price).toLocaleString()}` : '價格未知';
            const bookingId = booking.id || booking.number || '未知編號';
            const status = booking.status ? String(booking.status).toUpperCase() : 'UNKNOWN';
            let statusBadge = '';
            let actionButtonsHtml = '';
            let gpayPlaceholderHtml = '';

            switch (status) {
                case 'PAID':
                case 'CONFIRMED':
                    statusBadge = '<span class="badge bg-success">已付款</span>';
                    break;
                case 'PENDING':
                    statusBadge = '<span class="badge bg-warning text-dark">待付款</span>';
                    if (price !== null && !isNaN(price)) {
                        actionButtonsHtml += `<button class="btn btn-sm btn-primary pay-booking-btn w-100 mb-2" data-booking-id="${bookingId}" data-price="${price}"><i class="fab fa-google-pay me-1"></i>立即付款</button>`;
                        gpayPlaceholderHtml = `<div class="gpay-placeholder mt-2 w-100" style="min-height: 40px;"></div>`;
                    } else {
                        actionButtonsHtml += `<span class="text-danger small d-block w-100 text-end mb-2">價格錯誤</span>`;
                    }
                    actionButtonsHtml += `<button class="btn btn-sm btn-outline-danger delete-booking-btn w-100" data-booking-id="${bookingId}"><i class="fas fa-trash-alt me-1"></i>取消預訂</button>`;
                    break;
                case 'CANCELLED':
                    statusBadge = `<span class="badge bg-secondary">已取消</span>`;
                    break;
                default:
                    statusBadge = `<span class="badge bg-info text-dark">${escapeHtml(booking.status || '狀態未知')}</span>`;
                    break;
            }

            const bookingItemHtml = `
            <div class="list-group-item booking-item" data-booking-id="${bookingId}">
                <div class="row g-2 align-items-center">
                    <div class="col-auto">
                        <img src="${escapeHtml(imageUrl)}" alt="${escapeHtml(attractionName)}" class="img-fluid rounded" style="width: 80px; height: 60px; object-fit: cover;" onerror="this.onerror=null; this.src='https://placehold.co/80x60/E8E8E8/BDBDBD?text=Img+Err';">
                    </div>
                    <div class="col">
                        <h5 class="mb-1">${escapeHtml(attractionName)} ${statusBadge}</h5>
                        <p class="mb-1 small text-muted">預定編號: ${escapeHtml(String(bookingId))}</p>
                        <p class="mb-1">日期: ${dateStr}</p>
                        <p class="mb-1">時間: ${timeStr}</p>
                        <small>費用: ${priceStr}</small>
                    </div>
                    <div class="col-12 col-md-auto mt-2 mt-md-0">
                        <div class="d-flex flex-column align-items-stretch align-items-md-end" style="min-width: 100px;">
                            ${actionButtonsHtml}
                            ${gpayPlaceholderHtml}
                        </div>
                    </div>
                </div>
            </div>`;
            $myBookingsListContainer.append(bookingItemHtml);
        });
    }

    $myBookingsListContainer.on('click', '.pay-booking-btn', function() {
        storeFocus();
        const $button = $(this);
        const bookingId = $button.data('booking-id');
        const price = $button.data('price');
        const $bookingItem = $button.closest('.booking-item');
        const $gpayPlaceholder = $bookingItem.find('.gpay-placeholder');

        if (!bookingId || price === undefined || price === null || isNaN(price)) {
            showMessage($myBookingsMsgPlaceholder, '無法處理付款，訂單資訊或價格無效。', true);
            return;
        }
        currentPendingBookingId = bookingId;
        clearMessage($myBookingsMsgPlaceholder);

        $myBookingsListContainer.find('.gpay-placeholder').not($gpayPlaceholder).empty().addClass('hidden');
        $button.addClass('hidden');
        $bookingItem.find('.delete-booking-btn').addClass('hidden');
        $gpayPlaceholder.removeClass('hidden').empty();

        addGooglePayButton(price, 'TWD', $gpayPlaceholder);
    });

    $myBookingsListContainer.on('click', '.delete-booking-btn', function() {
        storeFocus();
        const $button = $(this);
        const $bookingItem = $button.closest('.booking-item');
        const bookingId = $bookingItem.data('booking-id');
        const tripName = $bookingItem.find('h5').contents().filter(function() {
            return this.nodeType === 3;
        }).text().trim() || '此行程';

        if (!bookingId) {
            showMessage($myBookingsMsgPlaceholder, '無法取消此預訂，缺少預訂 ID。', true);
            return;
        }
        openCancelBookingModal(bookingId, tripName);
    });

    function openCancelBookingModal(bookingId, tripName) {
        if (cancelBookingConfirmModal) {
            $('#cancelBookingId').val(bookingId);
            $('#cancelBookingIdDisplay').text(bookingId);
            $('#cancelBookingTripName').text(tripName);
            $(cancelBookingConfirmModalEl).css('z-index', parseInt($(myBookingsModalEl).css('z-index') || 1050) + 10);
            cancelBookingConfirmModal.show();
        } else {
            if (confirm(`您確定要取消預訂「${tripName}」(編號: ${bookingId}) 嗎？此操作無法復原。`)) {
                executeCancelBooking();
            }
        }
    }

    function executeCancelBooking() {
        const bookingId = $('#cancelBookingId').val();
        const $confirmButton = $('#confirmCancelBookingBtn');
        const $bookingItem = $myBookingsListContainer.find(`.booking-item[data-booking-id="${bookingId}"]`);
        const tripName = $('#cancelBookingTripName').text();

        if (!bookingId) {
            showMessage($myBookingsMsgPlaceholder, '無法取消此預訂，缺少預訂 ID。', true);
            if (cancelBookingConfirmModal) cancelBookingConfirmModal.hide();
            return;
        }
        const token = getToken();
        if (!token) {
            showMessage($myBookingsMsgPlaceholder, '您的登入已逾時，請重新登入後再試。', true);
            if (cancelBookingConfirmModal) cancelBookingConfirmModal.hide();
            if (myBookingsModal) myBookingsModal.hide();
            if (loginModal) { clearMessage($loginErrorPlaceholder); showMessage($loginErrorPlaceholder, '請重新登入', true); loginModal.show(); }
            return;
        }

        $.ajax({
            url: BOOKING_DELETE_URL(bookingId),
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` },
            dataType: 'json',
            beforeSend: function() {
                $confirmButton.prop('disabled', true).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 取消中...');
                clearMessage($myBookingsMsgPlaceholder);
            },
            success: function(response) {
                if (response && response.ok) {
                    if (cancelBookingConfirmModal) cancelBookingConfirmModal.hide();
                    $bookingItem.fadeOut(400, function() {
                        $(this).remove();
                        if ($myBookingsListContainer.children('.booking-item').length === 0) {
                            showMessage($myBookingsMsgPlaceholder, '您目前沒有任何預訂紀錄。', false);
                        } else {
                            showMessage($myBookingsMsgPlaceholder, `預訂「${tripName}」已成功取消。`, false);
                            setTimeout(() => clearMessage($myBookingsMsgPlaceholder), 3000);
                        }
                    });
                } else {
                    const errorMsg = (response && response.message) ? response.message : '取消預訂失敗。';
                    showMessage($myBookingsMsgPlaceholder, errorMsg, true);
                    if (cancelBookingConfirmModal) cancelBookingConfirmModal.hide();
                }
            },
            error: function(jqXHR) {
                let errorMsg = '取消預訂時發生錯誤。';
                if (jqXHR.status === 401 || jqXHR.status === 403) {
                    errorMsg = '您的登入已逾時或無權限取消此預訂。';
                    removeToken(); updateLoginStateUI(false);
                    if (cancelBookingConfirmModal) cancelBookingConfirmModal.hide();
                    if (myBookingsModal) myBookingsModal.hide();
                    if (loginModal) { clearMessage($loginErrorPlaceholder); showMessage($loginErrorPlaceholder, errorMsg, true); loginModal.show(); }
                } else if (jqXHR.status === 404) {
                    errorMsg = '找不到要取消的預訂。';
                } else if (jqXHR.status === 400) {
                     errorMsg = '無法取消此預訂，狀態可能已變更或不允許取消。';
                     setTimeout(() => $myBookingsBtn.trigger('click'), 1500);
                 } else if (jqXHR.status === 500) {
                    errorMsg = '伺服器內部錯誤，無法取消預訂。';
                } else if (jqXHR.status === 0) {
                     errorMsg = '無法連線至伺服器，請檢查網路連線。';
                 } else if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
                    errorMsg = jqXHR.responseJSON.message;
                } else {
                    errorMsg = `取消預訂失敗 (${jqXHR.status})。`;
                }
                showMessage($myBookingsMsgPlaceholder, errorMsg, true);
                if (cancelBookingConfirmModal) cancelBookingConfirmModal.hide();
            },
            complete: function() {
                $confirmButton.prop('disabled', false).text('確認取消');
            }
        });
    }
    $('#confirmCancelBookingBtn').on('click', executeCancelBooking);
    $('#cancelCancelBookingBtn').on('click', function() {
        if (cancelBookingConfirmModal) cancelBookingConfirmModal.hide();
    });

    $districtSelect.on('change', function() {
        const selectedDistrict = $(this).val();
        const currentKeyword = $searchInput.val().trim();
        loadTrips(0, currentKeyword, selectedDistrict, false);
    });

    $searchBtn.on('click', function() {
        const currentKeyword = $searchInput.val().trim();
        const selectedDistrict = $districtSelect.val();
        loadTrips(0, currentKeyword, selectedDistrict, false);
    });

    $searchInput.on('keypress', function(e) {
        if (e.which === 13) {
            e.preventDefault();
            $searchBtn.trigger('click');
        }
    });

    $tripListContainer.on('click', '.attraction-detail-trigger', function() {
        storeFocus();
        const $card = $(this).closest('.trip-card');
        const attractionId = $card.data('attraction-id');
        if (attractionId) {
            showAttractionDetails(attractionId);
        } else {
            showGeneralAlert("無法獲取景點詳細資訊。", "錯誤");
        }
    });

    function debounce(func, wait, immediate) {
        var timeout;
        return function() {
            var context = this, args = arguments;
            var later = function() {
                timeout = null;
                if (!immediate) func.apply(context, args);
            };
            var callNow = immediate && !timeout;
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
            if (callNow) func.apply(context, args);
        };
    };

    const handleScroll = debounce(function() {
        const scrollWrapper = $tripListContainer[0];
        const scrollThreshold = 300;
        const needsLoading = scrollWrapper.scrollWidth - scrollWrapper.scrollLeft - scrollWrapper.clientWidth < scrollThreshold;

        if (hasMoreTrips && !isLoadingTrips && needsLoading) {
             if (currentApiPage !== null) {
                 const currentKeyword = $searchInput.val().trim();
                 const selectedDistrict = $districtSelect.val();
                 loadTrips(currentApiPage, currentKeyword, selectedDistrict, true);
             }
        }
    }, 250);

    $tripListContainer.on('scroll', handleScroll);

    $(window).on('scroll', function() {
        if ($(window).scrollTop() > 300) {
            $backToTopBtn.fadeIn();
        } else {
            $backToTopBtn.fadeOut();
        }
    });

    $backToTopBtn.on('click', function(e) {
        e.preventDefault();
        $('html, body').animate({ scrollTop: 0 }, 300);
    });

    $('#viewMyBookingsAfterSuccess').on('click', function() {
        if ($myBookingsBtn.length) {
            $myBookingsBtn.trigger('click');
        }
        if (bookingSuccessModal) {
            bookingSuccessModal.hide();
        }
    });

    initializeGooglePay();
    loadDistricts();
    loadTrips();
    checkLoginStatus();

});

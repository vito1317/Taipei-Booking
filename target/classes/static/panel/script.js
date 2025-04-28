let bookingTrendChartInstance = null;
let userRoleChartInstance = null;

const API_BASE_URL = 'http://localhost:8080/api/admin';
const API_USER_AUTH_URL = 'http://localhost:8080/api/user/auth';
let currentAttractionsPage = 0;
let currentUserRole = null;

function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        const parsed = JSON.parse(jsonPayload);

        let role = null;
        if (parsed.authorities && Array.isArray(parsed.authorities) && parsed.authorities.length > 0) {
            role = parsed.authorities[0];
        } else if (parsed.role) {
             role = parsed.role;
        }

        if (role) {
            role = role.trim();
            if (!role.startsWith('ROLE_')) {
                role = 'ROLE_' + role;
            }
        }

        return { ...parsed, role: role || 'ROLE_USER' };

    } catch (e) {
        console.error("無法解析 JWT Token:", e);
        return { role: 'ROLE_USER' };
    }
}


function updateSidebarForRole(role) {
    let firstVisibleLink = null;
    const userRoleSimple = role ? role.replace('ROLE_', '') : '';

    $('#sidebarNav .sidebar-link').each(function() {
        const $link = $(this);
        const requiredRoles = ($link.data('role') || '').split(' ');
        let canShow = false;

        if (userRoleSimple && requiredRoles.includes(userRoleSimple)) {
            canShow = true;
        }
        else if (userRoleSimple === 'ADMIN' && (requiredRoles.includes('ADMIN') || requiredRoles.includes('TRIP_MANAGER'))) {
             canShow = true;
         }


        if (canShow) {
            $link.show();
            if (!firstVisibleLink) {
                firstVisibleLink = $link;
            }
        } else {
            $link.hide();
            $link.removeClass('active');
        }
    });

    $('.page-content').addClass('hidden');
    $('#sidebarNav .sidebar-link').removeClass('active');

    if (firstVisibleLink) {
        firstVisibleLink.addClass('active');
        const targetId = firstVisibleLink.attr('href').substring(1);
        $('#' + targetId + 'Content').removeClass('hidden');
        $('#pageTitle').text(firstVisibleLink.find('span').text().trim());
        loadContentForPage(targetId);
    } else {
        $('#pageTitle').text('無權限');
        showToast('權限不足，無法執行此操作。', 'error');
        logout();
    }
}


function loadContentForPage(pageId) {
     switch (pageId) {
        case 'dashboard':
            loadDashboardData();
            loadUserManagementData();
            break;
        case 'attractions':
            loadAttractions();
            break;
        case 'bookings':
            loadBookingManagementData();
            break;
        case 'users':
            loadUserManagementData();
            break;
    }
}


$(document).ready(function() {
    const $loginPage = $('#loginPage');
    const $adminDashboard = $('#adminDashboard');
    const $loginForm = $('#loginForm');
    const $logoutButton = $('#logoutButton');
    const $sidebar = $('#sidebar');
    const $sidebarToggle = $('#sidebarToggle');
    const $sidebarOverlay = $('#sidebarOverlay');
    const $sidebarLinks = $('#sidebarNav .sidebar-link');
    const $pageContents = $('.page-content');
    const $pageTitle = $('#pageTitle');

    const token = localStorage.getItem('jwtToken');

    if (token) {
        fetchUserRoleAndInitializeDashboard();
    } else {
        currentUserRole = null;
        showLoginPage();
        if ($loginForm.length) {
            $loginForm.on('submit', handleLogin);
        }
    }

    if ($logoutButton.length) {
        $logoutButton.on('click', logout);
    }

    $sidebarToggle.on('click', function() {
        $sidebar.toggleClass('-translate-x-full').toggleClass('translate-x-0');
        $sidebarOverlay.toggleClass('hidden');
    });

    $sidebarOverlay.on('click', function() {
        $sidebar.addClass('-translate-x-full').removeClass('translate-x-0');
        $(this).addClass('hidden');
    });

    $('#sidebarNav').on('click', '.sidebar-link', function(e) {
        e.preventDefault();
        const $thisLink = $(this);

        if (!$thisLink.is(':visible')) {
            return;
        }

        const targetId = $thisLink.attr('href').substring(1);

        $pageContents.addClass('hidden');

        const $targetContent = $('#' + targetId + 'Content');
        if ($targetContent.length) {
            $targetContent.removeClass('hidden');
            $pageTitle.text($thisLink.find('span').length ? $thisLink.find('span').text().trim() : $thisLink.text().trim());
        }

        $('#sidebarNav .sidebar-link').removeClass('active');
        $thisLink.addClass('active');

        if ($(window).width() < 768) {
             $sidebar.addClass('-translate-x-full').removeClass('translate-x-0');
             $sidebarOverlay.addClass('hidden');
        }

        loadContentForPage(targetId);
    });

});

function showLoginPage() {
    $('#loginPage').css('display', 'flex');
    $('#adminDashboard').hide();
    currentUserRole = null;
}

function showDashboard() {
    $('#loginPage').hide();
    $('#adminDashboard').css('display', 'flex');
    if ($(window).width() < 768) {
         $('#sidebar').addClass('-translate-x-full').removeClass('translate-x-0');
         $('#sidebarOverlay').addClass('hidden');
    }
}

function initializeDashboard() {
    const $adminDashboard = $('#adminDashboard');
    if (!$adminDashboard.length || $adminDashboard.css('display') === 'none') {
        return;
    }

    $('.page-content').addClass('hidden');
    $('#sidebarNav .sidebar-link').removeClass('active');

    if (currentUserRole) {
        updateSidebarForRole(currentUserRole);
    } else {
        console.error("無法獲取用戶角色，無法初始化儀表板。");
        logout();
    }
}

function initializeModals() {
    $(document).off('click.modalClose').on('click.modalClose', '.modal [id$="Btn"][id^="close"], .modal [id$="Btn"][id^="cancel"]', function() {
        const $modal = $(this).closest('.modal');
        if ($modal.length) {
            closeModal($modal.attr('id'));
        }
    });

    $(document).off('click.modalOverlay').on('click.modalOverlay', '.modal', function(event) {
        if (event.target === this) {
            closeModal($(this).attr('id'));
        }
    });
}

function initializePageSpecificEvents() {
    const $mainContentArea = $('main');

    $('#showAddAttractionModalBtn').off('click').on('click', function() { openAttractionModal(); });
    $('#attractionForm').off('submit').on('submit', handleSaveAttraction);
    $('#confirmDeleteAttractionBtn').off('click').on('click', executeDeleteAttraction);
    $('#attractionsPagination').off('click', 'button').on('click', 'button', function() {
        const page = $(this).data('page');
        if (page !== undefined && page !== currentAttractionsPage && !$(this).prop('disabled')) {
             loadAttractions(page);
         }
    });
    $mainContentArea.off('click.attractionActions').on('click.attractionActions', '#attractionsTableBody .edit-attraction-btn', function() {
        openAttractionModal($(this).data('attraction-id'));
    }).on('click.attractionActions', '#attractionsTableBody .toggle-attraction-status-btn', function() {
        handleToggleAttractionStatus($(this).data('attraction-id'), $(this).data('current-status'));
    }).on('click.attractionActions', '#attractionsTableBody .delete-attraction-btn', function() {
        confirmDeleteAttraction($(this).data('attraction-id'), $(this).data('attraction-name'));
    });

    $('#showAddUserModalBtn').off('click').on('click', function() { openModal('addUserModal'); });
    $('#editUserForm').off('submit').on('submit', handleEditUser);
    $('#addUserForm').off('submit').on('submit', handleAddUser);
    $('#confirmDeleteBtn').off('click').on('click', executeDeleteUser);
    $mainContentArea.off('click.userActions').on('click.userActions', '#usersTableBody .edit-user-btn', function() {
        openEditUserModal($(this).data('user-id'));
    }).on('click.userActions', '#usersTableBody .delete-user-btn', function() {
        confirmDeleteUser($(this).data('user-id'), $(this).data('user-name'));
    });

    $('#confirmDeleteBookingBtn').off('click').on('click', executeDeleteBooking);
    $mainContentArea.off('click.bookingActions').on('click.bookingActions', '#bookingsTableBody .view-booking-btn', function() {
        viewBookingDetails($(this).data('booking-id'));
    }).on('click.bookingActions', '#bookingsTableBody .delete-booking-btn', function() {
        confirmDeleteBookingModal($(this).data('booking-id'));
    });
}

async function fetchUserRoleAndInitializeDashboard() {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        logout();
        return;
    }
    try {
        const userInfo = await $.ajax({
            url: API_USER_AUTH_URL,
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            dataType: 'json'
        });

        currentUserRole = userInfo?.data?.role || 'ROLE_USER';

        showDashboard();
        initializeDashboard();
        initializeModals();
        initializePageSpecificEvents();
        showToast('登入成功！', 'success');

    } catch (error) {
        console.error("獲取用戶角色失敗:", error);
        showToast('無法獲取用戶資訊，請重新登入。', 'error');
        logout();
    }
}


function handleLogin(event) {
    event.preventDefault();
    const $usernameInput = $('#username');
    const $passwordInput = $('#password');
    const $loginErrorElement = $('#loginError');
    const $loginSubmitBtn = $('#loginSubmitBtn');
    const $loginSpinner = $('#loginSpinner');

    $loginErrorElement.text('');
    $loginSubmitBtn.prop('disabled', true);
    $loginSpinner.removeClass('hidden');

    const username = $usernameInput.val();
    const password = $passwordInput.val();

    if (!username || !password) {
        $loginErrorElement.text('請輸入帳號和密碼。');
        $loginSubmitBtn.prop('disabled', false);
        $loginSpinner.addClass('hidden');
        return;
    }

    $.ajax({
        url: 'http://localhost:8080/api/user/login',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ email: username, password: password }),
        dataType: 'json'
    })
    .done(function(data) {
        if (data.token) {
            localStorage.setItem('jwtToken', data.token);
            fetchUserRoleAndInitializeDashboard();
        } else {
            throw new Error('登入成功，但未收到 Token。');
        }
    })
    .fail(function(jqXHR) {
        let errorMessage = '登入失敗，請檢查您的帳號或密碼。';
        if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
            errorMessage = jqXHR.responseJSON.message;
        }
        $loginErrorElement.text(errorMessage);
        showToast(`登入失敗: ${errorMessage}`, 'error');
        currentUserRole = null;
    })
    .always(function() {
        $loginSubmitBtn.prop('disabled', false);
        $loginSpinner.addClass('hidden');
    });
}

function ajaxWithAuth(relativeUrl, options = {}) {
    const token = localStorage.getItem('jwtToken');
    const defaultOptions = {
        method: 'GET',
        contentType: 'application/json',
        dataType: 'json',
        headers: {}
    };

    const ajaxOptions = $.extend(true, {}, defaultOptions, options);

    if (token) {
        ajaxOptions.headers['Authorization'] = `Bearer ${token}`;
    } else {
        logout();
        return $.Deferred().reject(new Error('未授權，請重新登入。')).promise();
    }

    ajaxOptions.url = `${API_BASE_URL}${relativeUrl}`;

    return $.ajax(ajaxOptions)
        .fail(function(jqXHR) {
            if (jqXHR.status === 401) {
                 logout();
                 showToast('連線逾時或憑證無效，請重新登入。', 'error');
            } else if (jqXHR.status === 403) {
                showToast('權限不足，無法執行此操作。', 'error');
            } else {
                const errorMsg = (jqXHR.responseJSON && jqXHR.responseJSON.message) ? jqXHR.responseJSON.message : `請求失敗 (${jqXHR.status})，請檢查網絡或稍後再試。`;
                showToast(errorMsg, 'error');
            }
        });
}

async function loadDashboardData() {
    if (currentUserRole !== 'ROLE_ADMIN') {
        $('#dashboardContent').html('<p class="text-center text-gray-500 py-8">無儀表板訪問權限。</p>');
        return;
    }

    $('#totalUsers').text('載入中...');
    $('#totalBookings').text('載入中...');
    $('#totalRevenue').text('載入中...');
    $('#bookChartError').text('');
    $('#roleChartError').text('');

    try {
        const [stats, trends] = await Promise.all([
            ajaxWithAuth('/stats'),
            ajaxWithAuth('/trends/bookings')
        ]);

        displayDashboardStats(stats);
        renderBookingTrendChart(trends);
        loadUserManagementData();

    } catch (error) {
        $('#totalUsers').text('錯誤');
        $('#totalBookings').text('錯誤');
        $('#totalRevenue').text('錯誤');
        $('#bookChartError').text('無法加載預訂趨勢圖');
        $('#roleChartError').text('無法加載用戶角色圖');
        showToast(`加載儀表板數據失敗: ${error.message || '未知錯誤'}`, 'error');
    }
}

function displayDashboardStats(stats) {
    if (!stats) {
        $('#totalUsers').text('錯誤');
        $('#totalBookings').text('錯誤');
        $('#totalRevenue').text('錯誤');
        return;
    }
    $('#totalUsers').text(stats.totalUsers ?? 0);
    $('#totalBookings').text(stats.totalBookings ?? 0);

    const revenue = stats.totalRevenue;
    if (revenue !== null && revenue !== undefined && !isNaN(revenue)) {
        $('#totalRevenue').text(revenue.toLocaleString('zh-TW', {
            style: 'currency',
            currency: 'TWD',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }));
    } else {
        $('#totalRevenue').text('N/A');
    }
}

function renderBookingTrendChart(data) {
    const canvas = document.getElementById('bookingTrendChart');
    if (!canvas) {
        $('#bookChartError').text('無法初始化圖表 (Canvas不存在)');
        return;
    }
    const ctx = canvas.getContext('2d');
    const $errorElement = $('#bookChartError');
    $errorElement.text('');

    if (!data || data.length === 0) {
        $errorElement.text('沒有可用的預訂趨勢數據。');
        if (bookingTrendChartInstance) {
            bookingTrendChartInstance.destroy();
            bookingTrendChartInstance = null;
        }
        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
        ctx.textAlign = 'center';
        ctx.fillStyle = '#a0aec0';
        ctx.font = "14px 'Inter', 'Noto Sans TC', sans-serif";
        ctx.fillText('暫無數據', ctx.canvas.width / 2, ctx.canvas.height / 2);
        return;
    }

    const labels = data.map(d => d.label);
    const counts = data.map(d => d.value);

    if (bookingTrendChartInstance) {
        bookingTrendChartInstance.destroy();
    }

    try {
        bookingTrendChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: '每日預訂數',
                    data: counts,
                    borderColor: 'rgb(79, 70, 229)',
                    backgroundColor: 'rgba(79, 70, 229, 0.1)',
                    borderWidth: 2,
                    tension: 0.1,
                    fill: true,
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        backgroundColor: 'rgba(0, 0, 0, 0.7)',
                        titleFont: { size: 14 },
                        bodyFont: { size: 12 },
                        padding: 10,
                        cornerRadius: 4,
                    }
                }
            }
        });
    } catch (chartError) {
        $errorElement.text(`渲染圖表時出錯: ${chartError.message}`);
        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
        ctx.textAlign = 'center';
        ctx.fillStyle = '#ef4444';
        ctx.font = "14px 'Inter', 'Noto Sans TC', sans-serif";
        ctx.fillText('圖表渲染錯誤', ctx.canvas.width / 2, ctx.canvas.height / 2);
    }
}

async function loadUserManagementData() {
    if (currentUserRole !== 'ROLE_ADMIN') {
        $('#usersContent').html('<p class="text-center text-gray-500 py-8">無帳號管理權限。</p>');
        const canvas = document.getElementById('userRoleChart');
         if(canvas) {
             const ctx = canvas.getContext('2d');
             if (userRoleChartInstance) {
                 userRoleChartInstance.destroy();
                 userRoleChartInstance = null;
             }
             ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
             ctx.textAlign = 'center';
             ctx.fillStyle = '#a0aec0';
             ctx.font = "14px 'Inter', 'Noto Sans TC', sans-serif";
             ctx.fillText('無權限查看', ctx.canvas.width / 2, ctx.canvas.height / 2);
         }
         $('#roleChartError').text('');
        return;
    }

    const $userListBody = $('#usersTableBody');
    const $roleChartError = $('#roleChartError');
    const isUserPageVisible = $('#usersContent').is(':visible');
    const isDashboardVisible = $('#dashboardContent').is(':visible');

    if (isUserPageVisible && $userListBody.length) {
        $userListBody.html(`<tr><td colspan="6" class="text-center py-4 text-gray-500">載入中...</td></tr>`);
    }
    if (isDashboardVisible && $roleChartError.length) {
        $roleChartError.text('');
    }

    try {
        const usersPage = await ajaxWithAuth('/users', { data: { page: 0, size: 1000 } });
        const users = usersPage?.content;

        if (!users) {
            throw new Error('無法獲取有效的用戶數據');
        }

        if (isUserPageVisible && $userListBody.length) {
            displayUserList(users);
        }
        if ($('#userRoleChart').length) {
            renderUserRoleChart(users);
        }

    } catch (error) {
        const errorMsg = `無法加載用戶數據: ${error.message || '未知錯誤'}`;
        if (isUserPageVisible && $userListBody.length) {
            $userListBody.html(`<tr><td colspan="6" class="text-center py-4 text-red-500">${errorMsg}</td></tr>`);
        }
        if ($roleChartError.length) {
            $roleChartError.text(errorMsg.replace('用戶數據', '用戶角色圖'));
            const canvas = document.getElementById('userRoleChart');
            if(canvas) {
                const ctx = canvas.getContext('2d');
                if (userRoleChartInstance) {
                    userRoleChartInstance.destroy();
                    userRoleChartInstance = null;
                }
                ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
                ctx.textAlign = 'center';
                ctx.fillStyle = '#ef4444';
                ctx.font = "14px 'Inter', 'Noto Sans TC', sans-serif";
                ctx.fillText('圖表加載錯誤', ctx.canvas.width / 2, ctx.canvas.height / 2);
            }
        }
    }
}

function displayUserList(users) {
    const $userListBody = $('#usersTableBody');
    if (!$userListBody.length) return;

    $userListBody.empty();

    if (!users || users.length === 0) {
        $userListBody.html('<tr><td colspan="6" class="text-center py-4 text-gray-500">目前沒有用戶</td></tr>');
        return;
    }

    users.forEach(user => {
        const registrationDate = user.createdAt ?
            new Date(user.createdAt).toLocaleString('zh-TW', {
                year: 'numeric', month: '2-digit', day: '2-digit',
                hour: '2-digit', minute: '2-digit'
            }) : 'N/A';

        const userName = user.name || 'N/A';
        const userEmail = user.email || 'N/A';

        let roleClass = 'bg-blue-100 text-blue-700';
        let roleText = '一般用戶';
        if (user.role === 'ROLE_ADMIN') {
            roleClass = 'bg-red-100 text-red-700';
            roleText = '管理員';
        } else if (user.role === 'ROLE_TRIP_MANAGER') {
            roleClass = 'bg-yellow-100 text-yellow-700';
            roleText = '行程管理員';
        }

        const $row = $('<tr>').addClass('border-b hover:bg-gray-50');

        $row.html(`
            <td class="py-3 px-4 text-sm text-gray-700 hidden lg:table-cell">${user.id}</td>
            <td class="py-3 px-4 text-sm text-gray-900 font-medium">${userName}</td>
            <td class="py-3 px-4 text-sm text-gray-700 hidden sm:table-cell break-all">${userEmail}</td>
            <td class="py-3 px-4 text-sm">
                <span class="inline-block whitespace-nowrap px-2 py-1 text-xs font-semibold leading-tight rounded-full ${roleClass}">
                    ${roleText}
                </span>
            </td>
            <td class="py-3 px-4 text-sm text-gray-700 hidden md:table-cell">${registrationDate}</td>
            <td class="py-3 px-4 text-sm whitespace-nowrap">
                <div class="flex items-center space-x-2">
                    <button class="edit-user-btn text-indigo-600 hover:text-indigo-800 font-medium transition duration-150 ease-in-out p-1" data-user-id="${user.id}" title="編輯用戶">
                        <i class="fas fa-pencil-alt w-4 h-4"></i>
                        <span class="hidden sm:inline ml-1">編輯</span>
                    </button>
                    <button class="delete-user-btn text-red-600 hover:text-red-800 font-medium transition duration-150 ease-in-out p-1" data-user-id="${user.id}" data-user-name="${userName}" title="刪除用戶">
                        <i class="fas fa-trash-alt w-4 h-4"></i>
                        <span class="hidden sm:inline ml-1">刪除</span>
                    </button>
                </div>
            </td>
        `);
        $userListBody.append($row);
    });
}

function renderUserRoleChart(users) {
    const canvas = document.getElementById('userRoleChart');
    if (!canvas) {
        $('#roleChartError').text('');
        return;
    }
    const ctx = canvas.getContext('2d');
    const $errorElement = $('#roleChartError');
    $errorElement.text('');

    if (!users || users.length === 0) {
        $errorElement.text('沒有可用的用戶數據來生成圖表。');
        if (userRoleChartInstance) {
            userRoleChartInstance.destroy();
            userRoleChartInstance = null;
        }
        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
        ctx.textAlign = 'center';
        ctx.fillStyle = '#a0aec0';
        ctx.font = "14px 'Inter', 'Noto Sans TC', sans-serif";
        ctx.fillText('暫無數據', ctx.canvas.width / 2, ctx.canvas.height / 2);
        return;
    }

    const roleCounts = users.reduce((acc, user) => {
        const role = user.role || 'UNKNOWN';
        acc[role] = (acc[role] || 0) + 1;
        return acc;
    }, {});

    const labels = Object.keys(roleCounts).map(role =>
        role === 'ROLE_ADMIN' ? '管理員' :
        (role === 'ROLE_TRIP_MANAGER' ? '行程管理員' :
        (role === 'ROLE_USER' ? '一般用戶' : '未知角色'))
    );
    const data = Object.values(roleCounts);

    const backgroundColors = Object.keys(roleCounts).map(role =>
        role === 'ROLE_ADMIN' ? 'rgba(239, 68, 68, 0.7)' :
        (role === 'ROLE_TRIP_MANAGER' ? 'rgba(245, 158, 11, 0.7)' :
        (role === 'ROLE_USER' ? 'rgba(59, 130, 246, 0.7)' :
         'rgba(156, 163, 175, 0.7)'))
    );
    const borderColors = Object.keys(roleCounts).map(role =>
        role === 'ROLE_ADMIN' ? 'rgb(239, 68, 68)' :
        (role === 'ROLE_TRIP_MANAGER' ? 'rgb(245, 158, 11)' :
        (role === 'ROLE_USER' ? 'rgb(59, 130, 246)' :
         'rgb(156, 163, 175)'))
    );

    if (userRoleChartInstance) {
        userRoleChartInstance.destroy();
    }

    try {
        userRoleChartInstance = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    label: '用戶數量',
                    data: data,
                    backgroundColor: backgroundColors,
                    borderColor: borderColors,
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            font: {
                                size: 12,
                                family: "'Inter', 'Noto Sans TC', sans-serif"
                            }
                        }
                    },
                    tooltip: {
                        backgroundColor: 'rgba(0, 0, 0, 0.7)',
                        titleFont: { size: 14 },
                        bodyFont: { size: 12 },
                        padding: 10,
                        cornerRadius: 4,
                        callbacks: {
                            label: function(context) {
                                let label = context.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                if (context.parsed !== null) {
                                    label += context.parsed + ' 人';
                                }
                                return label;
                            }
                        }
                    }
                }
            }
        });
    } catch (chartError) {
        $errorElement.text(`渲染圖表時出錯: ${chartError.message}`);
        ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
        ctx.textAlign = 'center';
        ctx.fillStyle = '#ef4444';
        ctx.font = "14px 'Inter', 'Noto Sans TC', sans-serif";
        ctx.fillText('圖表渲染錯誤', ctx.canvas.width / 2, ctx.canvas.height / 2);
    }
}

function openModal(modalId) {
    const $modal = $('#' + modalId);
    if ($modal.length) {
        $modal.removeClass('hidden').addClass('flex');
        setTimeout(() => {
            const $modalContent = $modal.find('.modal-content');
            if ($modalContent.length) {
                $modalContent.removeClass('scale-95 opacity-0').addClass('scale-100 opacity-100');
            }
        }, 10);

        $modal.find('#editError, #addError, #bookingDetailError, #attractionError').text('');

        if (modalId === 'bookingDetailModal') {
            $('#bookingDetailLoading').removeClass('hidden');
            $('#bookingDetailError').addClass('hidden');
            $('#bookingDetailData').addClass('hidden');
        }
    }
}

function closeModal(modalId) {
    const $modal = $('#' + modalId);
    if ($modal.length && !$modal.hasClass('hidden')) {
        const $modalContent = $modal.find('.modal-content');

        if ($modalContent.length) {
            $modalContent.removeClass('scale-100 opacity-100').addClass('scale-95 opacity-0');
        } else {
            $modal.addClass('opacity-0');
        }

        setTimeout(() => {
            $modal.addClass('hidden').removeClass('flex');
            if (!$modalContent.length) $modal.removeClass('opacity-0');

            const $form = $modal.find('form');
            if ($form.length > 0 && $form[0] instanceof HTMLFormElement) {
                try {
                    $form[0].reset();
                    clearFormErrors($form[0]);
                } catch (e) {
                    console.warn(`無法重置 Modal (${modalId}) 中的表單:`, e);
                }
            }

            if (modalId === 'editUserModal') $('#editUserId').val('');
            if (modalId === 'deleteConfirmModal') { $('#deleteUserId').val(''); $('#deleteUserName').text(''); }
            if (modalId === 'deleteBookingConfirmModal') { $('#deleteBookingId').val(''); $('#deleteBookingIdDisplay').text(''); }
            if (modalId === 'attractionModal') { $('#attractionId').val(''); }
            if (modalId === 'deleteAttractionConfirmModal') { $('#deleteAttractionId').val(''); $('#deleteAttractionName').text(''); }

        }, 300);
    }
}

async function handleAddUser(event) {
    event.preventDefault();
    const $form = $(this);
    const $errorElement = $('#addError');
    const $saveButton = $form.find('#saveAddBtn');

    $errorElement.text('');
    $saveButton.prop('disabled', true);

    const apiData = {
        name: $('#addUserName').val(),
        email: $('#addUserEmail').val(),
        password: $('#addUserPassword').val(),
        role: $('#addUserRole').val()
    };

    if (!apiData.name || !apiData.email || !apiData.password) {
        $errorElement.text('請填寫所有必填欄位。');
        $saveButton.prop('disabled', false);
        return;
    }
    if (apiData.password.length < 6) {
        $errorElement.text('密碼長度至少需要 6 位。');
        $saveButton.prop('disabled', false);
        return;
    }
    if (!apiData.role) {
        $errorElement.text('請選擇用戶角色。');
        $saveButton.prop('disabled', false);
        return;
    }

    try {
        await ajaxWithAuth('/users', {
            method: 'POST',
            data: JSON.stringify(apiData)
        });
        showToast('用戶創建成功', 'success');
        closeModal('addUserModal');
        if ($('#usersContent').is(':visible') || $('#dashboardContent').is(':visible')) {
            loadUserManagementData();
        }
    } catch (error) {
        const errorMessage = (error.responseJSON && error.responseJSON.message) ? error.responseJSON.message : '創建用戶失敗，請稍後再試';
        $errorElement.text(errorMessage);
        showToast(`錯誤: ${errorMessage}`, 'error');
    } finally {
        $saveButton.prop('disabled', false);
    }
}

async function openEditUserModal(userId) {
    const $errorElement = $('#editError');
    $errorElement.text('');

    try {
        const user = await ajaxWithAuth(`/users/${userId}`);
        $('#editUserId').val(user.id);
        $('#editUserName').val(user.name || '');
        $('#editUserEmail').val(user.email);
        $('#editUserRole').val(user.role);
        openModal('editUserModal');
    } catch (error) {
        showToast(`無法加載用戶 ${userId} 的資料: ${error.message || '未知錯誤'}`, 'error');
    }
}

async function handleEditUser(event) {
    event.preventDefault();
    const $form = $(this);
    const $errorElement = $('#editError');
    const $saveButton = $form.find('#saveEditBtn');

    $errorElement.text('');
    $saveButton.prop('disabled', true);

    const userId = $('#editUserId').val();
    if (!userId) {
        showToast('無法獲取用戶 ID', 'error');
        $errorElement.text('無法獲取用戶 ID。');
        $saveButton.prop('disabled', false);
        return;
    }

    const apiData = {
        name: $('#editUserName').val(),
        email: $('#editUserEmail').val(),
        role: $('#editUserRole').val()
    };

    if (!apiData.name || !apiData.email) {
        $errorElement.text('姓名和 Email 不能為空。');
        $saveButton.prop('disabled', false);
        return;
    }
     if (!apiData.role) {
        $errorElement.text('請選擇用戶角色。');
        $saveButton.prop('disabled', false);
        return;
    }

    try {
        await ajaxWithAuth(`/users/${userId}`, {
            method: 'PUT',
            data: JSON.stringify(apiData)
        });
        showToast('用戶更新成功', 'success');
        closeModal('editUserModal');
        if ($('#usersContent').is(':visible') || $('#dashboardContent').is(':visible')) {
            loadUserManagementData();
        }
    } catch (error) {
        const errorMessage = (error.responseJSON && error.responseJSON.message) ? error.responseJSON.message : '更新用戶失敗，請稍後再試';
        $errorElement.text(errorMessage);
        showToast(`錯誤: ${errorMessage}`, 'error');
    } finally {
        $saveButton.prop('disabled', false);
    }
}

function confirmDeleteUser(userId, username) {
    $('#deleteUserId').val(userId);
    $('#deleteUserName').text(username || `ID: ${userId}`);
    openModal('deleteConfirmModal');
}

async function executeDeleteUser() {
    const userId = $('#deleteUserId').val();
    const $confirmButton = $('#confirmDeleteBtn');

    if (!userId) {
        showToast('無法獲取用戶 ID', 'error');
        closeModal('deleteConfirmModal');
        return;
    }

    $confirmButton.prop('disabled', true);

    try {
        await ajaxWithAuth(`/users/${userId}`, { method: 'DELETE' });
        showToast('用戶刪除成功', 'success');
        closeModal('deleteConfirmModal');
        if ($('#usersContent').is(':visible') || $('#dashboardContent').is(':visible')) {
            loadUserManagementData();
        }
    } catch (error) {
        showToast(`刪除用戶失敗: ${(error.responseJSON?.message || '請稍後再試')}`, 'error');
        closeModal('deleteConfirmModal');
    } finally {
        $confirmButton.prop('disabled', false);
    }
}

async function loadBookingManagementData() {
    if (currentUserRole !== 'ROLE_ADMIN') {
        $('#bookingsContent').html('<p class="text-center text-gray-500 py-8">無預訂管理權限。</p>');
        return;
    }

    const $tableBody = $('#bookingsTableBody');
    if (!$tableBody.length || !$tableBody.closest('#bookingsContent').is(':visible')) {
        return;
    }

    $tableBody.html(`<tr><td colspan="9" class="text-center py-4 text-gray-500">載入中...</td></tr>`);

    try {
        const resultPage = await ajaxWithAuth('/bookings', { data: { page: 0, size: 100 } });
        const bookings = resultPage?.content;

        if (!bookings) {
            throw new Error('無法獲取有效的預訂數據');
        }
        displayBookingList(bookings);
    } catch (error) {
        $tableBody.html(`<tr><td colspan="9" class="text-center py-4 text-red-500">無法加載預訂數據: ${error.message || '未知錯誤'}</td></tr>`);
    }
}

function displayBookingList(bookings) {
    const $tableBody = $('#bookingsTableBody');
    if (!$tableBody.length) return;

    $tableBody.empty();

    if (!bookings || bookings.length === 0) {
        $tableBody.html('<tr><td colspan="9" class="text-center py-4 text-gray-500">目前沒有任何預訂記錄</td></tr>');
        return;
    }

    bookings.forEach(booking => {
        const bookingDate = booking.date ? new Date(booking.date).toLocaleDateString('zh-TW') : 'N/A';
        const bookingTime = booking.time === 'morning' ? '早上' : (booking.time === 'afternoon' ? '下午' : 'N/A');
        const createdAt = booking.createdAt ? new Date(booking.createdAt).toLocaleString('zh-TW') : 'N/A';
        const price = booking.price ? booking.price.toLocaleString('zh-TW') : 'N/A';
        const status = booking.status || '-';
        const userName = booking.customerName || 'N/A';
        const attractionName = booking.attractionName || 'N/A';

        let statusClass = 'bg-gray-100 text-gray-700';
        let statusText = status;
        if (status === 'PAID' || status === '已付款') {
            statusClass = 'bg-green-100 text-green-700';
            statusText = '已付款';
        } else if (status === 'PENDING' || status === '待付款') {
            statusClass = 'bg-yellow-100 text-yellow-700';
            statusText = '待付款';
        } else {
            statusText = '未知';
        }

        const $row = $('<tr>').addClass('border-b hover:bg-gray-50');

        $row.html(`
            <td class="py-3 px-4 text-sm text-gray-700 hidden lg:table-cell">${booking.id}</td>
            <td class="py-3 px-4 text-sm text-gray-900 font-medium">${userName}</td>
            <td class="py-3 px-4 text-sm text-gray-700">${attractionName}</td>
            <td class="py-3 px-4 text-sm text-gray-700 hidden sm:table-cell">${bookingDate}</td>
            <td class="py-3 px-4 text-sm text-gray-700 hidden md:table-cell">${bookingTime}</td>
            <td class="py-3 px-4 text-sm text-gray-700 text-right">${price}</td>
            <td class="py-3 px-4 text-sm">
                <span class="inline-block whitespace-nowrap px-2 py-1 text-xs font-semibold leading-tight rounded-full ${statusClass}">
                    ${statusText}
                </span>
            </td>
            <td class="py-3 px-4 text-sm text-gray-700 hidden lg:table-cell">${createdAt}</td>
            <td class="py-3 px-4 text-sm whitespace-nowrap">
                <div class="flex items-center space-x-2">
                    <button class="view-booking-btn text-blue-600 hover:text-blue-800 font-medium transition duration-150 ease-in-out p-1" data-booking-id="${booking.id}" title="查看詳情">
                        <i class="fas fa-eye w-4 h-4"></i>
                        <span class="hidden sm:inline ml-1">詳情</span>
                    </button>
                    ${status !== 'PAID' && status !== '已付款' ? `
                    <button class="delete-booking-btn text-red-600 hover:text-red-800 font-medium transition duration-150 ease-in-out p-1" data-booking-id="${booking.id}" title="刪除預訂">
                        <i class="fas fa-trash-alt w-4 h-4"></i>
                        <span class="hidden sm:inline ml-1">刪除</span>
                    </button>` : ''}
                </div>
            </td>
        `);
        $tableBody.append($row);
    });
}

async function viewBookingDetails(bookingId) {
    if (currentUserRole !== 'ROLE_ADMIN') {
        showToast('權限不足，無法查看預訂詳情。', 'error');
        return;
    }

    openModal('bookingDetailModal');
    const $loadingElement = $('#bookingDetailLoading');
    const $errorElement = $('#bookingDetailError');
    const $dataElement = $('#bookingDetailData');

    $loadingElement.removeClass('hidden');
    $errorElement.addClass('hidden').text('');
    $dataElement.addClass('hidden');

    try {
        const bookingDetails = await ajaxWithAuth(`/bookings/${bookingId}`);

        if (!bookingDetails) {
            throw new Error('無法獲取有效的預訂詳情');
        }

        $('#detailBookingId').text(bookingDetails.id || 'N/A');
        $('#detailCreatedAt').text(bookingDetails.createdAt ? new Date(bookingDetails.createdAt).toLocaleString('zh-TW') : 'N/A');
        $('#detailPrice').text(bookingDetails.price?.toLocaleString('zh-TW') || 'N/A');

        const $statusSpan = $('#detailBookingStatus');
        const statusText = bookingDetails.status || '未知';
        let statusClass = 'bg-gray-100 text-gray-700';
        let displayStatusText = '未知';
        if (statusText === 'PAID' || statusText === '已付款') {
            statusClass = 'bg-green-100 text-green-700';
            displayStatusText = '已付款';
        } else if (statusText === 'PENDING' || statusText === '待付款') {
            statusClass = 'bg-yellow-100 text-yellow-700';
            displayStatusText = '待付款';
        }
        $statusSpan.text(displayStatusText)
                   .removeClass('bg-green-100 text-green-700 bg-yellow-100 text-yellow-700 bg-gray-100 text-gray-700')
                   .addClass(statusClass);

        $('#detailCustomerName').text(bookingDetails.customerName || 'N/A');
        $('#detailCustomerEmail').text(bookingDetails.customerEmail || 'N/A');
        $('#detailContactPhone').text(bookingDetails.contactPhone || 'N/A');
        $('#detailCustomerIdNumber').text(bookingDetails.customerIdNumber || 'N/A');
        $('#detailAttractionName').text(bookingDetails.attractionName || 'N/A');
        $('#detailAttractionAddress').text(bookingDetails.attractionAddress || 'N/A');
        $('#detailBookingDate').text(bookingDetails.date ? new Date(bookingDetails.date).toLocaleDateString('zh-TW') : 'N/A');
        $('#detailBookingTime').text(bookingDetails.time === 'morning' ? '早上' : (bookingDetails.time === 'afternoon' ? '下午' : 'N/A'));

        const $imgElement = $('#detailAttractionImage');
        const $imgErrorSpan = $imgElement.next();
        if (bookingDetails.attractionImage) {
            let imagePath = bookingDetails.attractionImage;
            if (!imagePath.startsWith('/') && !imagePath.startsWith('http')) {
                 imagePath = '../' + imagePath;
            }
            $imgElement.attr('src', imagePath).show();
            $imgErrorSpan.hide().text('無法載入圖片');
        } else {
            $imgElement.hide().attr('src', '');
            $imgErrorSpan.show().text('無圖片資訊');
        }

        $loadingElement.addClass('hidden');
        $errorElement.addClass('hidden');
        $dataElement.removeClass('hidden');

    } catch (error) {
        $loadingElement.addClass('hidden');
        $dataElement.addClass('hidden');
        $errorElement.text(`無法加載預訂詳情: ${error.message || '未知錯誤'}`).removeClass('hidden');
    }
}

function confirmDeleteBookingModal(bookingId) {
    if (currentUserRole !== 'ROLE_ADMIN') {
        showToast('權限不足，無法刪除預訂。', 'error');
        return;
    }
    $('#deleteBookingId').val(bookingId);
    $('#deleteBookingIdDisplay').text(bookingId);
    openModal('deleteBookingConfirmModal');
}

async function executeDeleteBooking() {
    const bookingId = $('#deleteBookingId').val();
    const $confirmButton = $('#confirmDeleteBookingBtn');

    if (!bookingId) {
        showToast('無法獲取預訂 ID', 'error');
        closeModal('deleteBookingConfirmModal');
        return;
    }

    $confirmButton.prop('disabled', true);

    try {
        await ajaxWithAuth(`/bookings/${bookingId}`, { method: 'DELETE' });
        showToast('預訂刪除成功', 'success');
        closeModal('deleteBookingConfirmModal');
        loadBookingManagementData();
    } catch (error) {
        showToast(`刪除預訂 ${bookingId} 失敗: ${error.responseJSON?.message || '請稍後再試'}`, 'error');
        closeModal('deleteBookingConfirmModal');
    } finally {
        $confirmButton.prop('disabled', false);
    }
}

async function loadAttractions(page = 0) {
    if (currentUserRole !== 'ROLE_ADMIN' && currentUserRole !== 'ROLE_TRIP_MANAGER') {
         $('#attractionsContent').html('<p class="text-center text-gray-500 py-8">無行程管理權限。</p>');
        return;
    }

    const $tableBody = $('#attractionsTableBody');
    const $paginationContainer = $('#attractionsPagination');

    if (!$tableBody.length || !$tableBody.closest('#attractionsContent').is(':visible')) {
        return;
    }

    currentAttractionsPage = page;

    $tableBody.html(`<tr><td colspan="6" class="text-center py-4 text-gray-500">載入中...</td></tr>`);
    $paginationContainer.empty();

    try {
        const resultPage = await ajaxWithAuth('/attractions', {
            data: {
                page: currentAttractionsPage,
                size: 10,
                sort: 'id,asc'
            }
        });

        if (resultPage && resultPage.content) {
            displayAttractionsList(resultPage.content);
            renderPagination($paginationContainer, resultPage);
        } else {
            throw new Error('無法獲取有效的行程數據');
        }
    } catch (error) {
        if (error.message && !error.message.includes('權限不足')) {
            $tableBody.html(`<tr><td colspan="6" class="text-center py-4 text-red-500">無法加載行程數據: ${error.message || '未知錯誤'}</td></tr>`);
        }
    }
}

function displayAttractionsList(attractions) {
    const $tableBody = $('#attractionsTableBody');
    if (!$tableBody.length) return;

    $tableBody.empty();

    if (!attractions || attractions.length === 0) {
        $tableBody.html('<tr><td colspan="6" class="text-center py-4 text-gray-500">目前沒有任何行程</td></tr>');
        return;
    }

    attractions.forEach(att => {
        const statusText = att.isActive ? '啟用中' : '已停用';
        const statusClass = att.isActive ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700';
        const toggleText = att.isActive ? '停用' : '啟用';
        const toggleIcon = att.isActive ? 'fa-toggle-off' : 'fa-toggle-on';
        const toggleBtnClass = att.isActive ? 'text-gray-500 hover:text-gray-700' : 'text-green-600 hover:text-green-800';

        const $row = $('<tr>').addClass('border-b hover:bg-gray-50');

        $row.html(`
            <td class="py-3 px-4 text-sm text-gray-700 hidden lg:table-cell">${att.id}</td>
            <td class="py-3 px-4 text-sm text-gray-900 font-medium">${att.name || 'N/A'}</td>
            <td class="py-3 px-4 text-sm text-gray-700 hidden md:table-cell">${att.category || '-'}</td>
            <td class="py-3 px-4 text-sm text-gray-700 hidden lg:table-cell">${att.mrt || '-'}</td>
            <td class="py-3 px-4 text-sm text-center">
                <span class="inline-block whitespace-nowrap px-2 py-1 text-xs font-semibold leading-tight rounded-full ${statusClass}">
                    ${statusText}
                </span>
            </td>
            <td class="py-3 px-4 text-sm whitespace-nowrap">
                <div class="flex items-center space-x-2">
                    <button class="edit-attraction-btn text-indigo-600 hover:text-indigo-800 font-medium p-1" data-attraction-id="${att.id}" title="編輯行程">
                        <i class="fas fa-pencil-alt w-4 h-4"></i>
                        <span class="hidden sm:inline ml-1">編輯</span>
                    </button>
                    <button class="toggle-attraction-status-btn ${toggleBtnClass} font-medium p-1" data-attraction-id="${att.id}" data-current-status="${att.isActive}" title="${toggleText}行程">
                        <i class="fas ${toggleIcon} w-4 h-4"></i>
                        <span class="hidden sm:inline ml-1">${toggleText}</span>
                    </button>
                    <button class="delete-attraction-btn text-red-600 hover:text-red-800 font-medium p-1" data-attraction-id="${att.id}" data-attraction-name="${att.name || ''}" title="刪除行程">
                        <i class="fas fa-trash-alt w-4 h-4"></i>
                        <span class="hidden sm:inline ml-1">刪除</span>
                    </button>
                </div>
            </td>
        `);
        $tableBody.append($row);
    });
}

function renderPagination($container, pageData) {
    $container.empty();

    if (!pageData || pageData.totalPages <= 1) return;

    const currentPage = pageData.number;
    const totalPages = pageData.totalPages;
    const maxVisiblePages = 5;

    let paginationHtml = '<nav><ul class="inline-flex items-center -space-x-px">';

    paginationHtml += `<li><button data-page="${currentPage - 1}" class="pagination-btn ${currentPage === 0 ? 'disabled' : ''}" ${currentPage === 0 ? 'disabled' : ''}><span class="sr-only">上一頁</span><i class="fas fa-chevron-left h-4 w-4"></i></button></li>`;

    let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

    if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }

    if (startPage > 0) {
        paginationHtml += `<li><button data-page="0" class="pagination-btn">1</button></li>`;
        if (startPage > 1) {
             paginationHtml += `<li><span class="pagination-ellipsis">...</span></li>`;
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        paginationHtml += `<li><button data-page="${i}" class="pagination-btn ${i === currentPage ? 'active' : ''}">${i + 1}</button></li>`;
    }

    if (endPage < totalPages - 1) {
         if (endPage < totalPages - 2) {
             paginationHtml += `<li><span class="pagination-ellipsis">...</span></li>`;
         }
         paginationHtml += `<li><button data-page="${totalPages - 1}" class="pagination-btn">${totalPages}</button></li>`;
    }

    paginationHtml += `<li><button data-page="${currentPage + 1}" class="pagination-btn ${currentPage === totalPages - 1 ? 'disabled' : ''}" ${currentPage === totalPages - 1 ? 'disabled' : ''}><span class="sr-only">下一頁</span><i class="fas fa-chevron-right h-4 w-4"></i></button></li>`;

    paginationHtml += '</ul></nav>';
    $container.html(paginationHtml);

    $container.find('.pagination-btn').addClass('py-2 px-3 leading-tight text-gray-500 bg-white border border-gray-300 hover:bg-gray-100 hover:text-gray-700');
    $container.find('.pagination-btn.active').addClass('z-10 text-blue-600 border-blue-300 bg-blue-50 hover:bg-blue-100 hover:text-blue-700').removeClass('text-gray-500 bg-white');
    $container.find('.pagination-btn.disabled').addClass('opacity-50 cursor-not-allowed');
    $container.find('.pagination-ellipsis').addClass('py-2 px-3 leading-tight text-gray-500 bg-white border border-gray-300');
    $container.find('li:first-child button').addClass('rounded-l-lg');
    $container.find('li:last-child button').addClass('rounded-r-lg');
}


async function openAttractionModal(attractionId = null) {
    if (currentUserRole !== 'ROLE_ADMIN' && currentUserRole !== 'ROLE_TRIP_MANAGER') {
         showToast('權限不足，無法執行此操作。', 'error');
        return;
    }

    const isEditMode = attractionId !== null;
    $('#attractionModalTitle').text(isEditMode ? '編輯行程' : '新增行程');
    $('#attractionForm')[0].reset();
    $('#attractionId').val(attractionId || '');
    clearFormErrors($('#attractionForm')[0]);
    $('#attractionError').text('');

    if (isEditMode) {
        try {
            const attraction = await ajaxWithAuth(`/attractions/${attractionId}`);
            $('#attractionName').val(attraction.name || '');
            $('#attractionDescription').val(attraction.description || '');
            $('#attractionAddress').val(attraction.address || '');
            $('#attractionImageUrl').val(attraction.imageUrl || '');
            $('#attractionMrt').val(attraction.mrt || '');
            $('#attractionCategory').val(attraction.category || '');
            if (attraction.isActive === false) {
                $('#attractionIsActiveFalse').prop('checked', true);
            } else {
                $('#attractionIsActiveTrue').prop('checked', true);
            }
            openModal('attractionModal');
        } catch (error) {
             if (!error.message || !error.message.includes('權限不足')) {
                showToast(`無法載入行程 ${attractionId} 的資料: ${error.message || '未知錯誤'}`, 'error');
            }
        }
    } else {
        $('#attractionIsActiveTrue').prop('checked', true);
        openModal('attractionModal');
    }
}

async function handleSaveAttraction(event) {
    event.preventDefault();
    const $form = $(this);
    const $errorElement = $('#attractionError');
    const $saveButton = $('#saveAttractionBtn');

    $errorElement.text('');
    $saveButton.prop('disabled', true);

    const attractionId = $('#attractionId').val();
    const isEditMode = !!attractionId;

    const apiData = {
        name: $('#attractionName').val().trim(),
        description: $('#attractionDescription').val().trim(),
        address: $('#attractionAddress').val().trim(),
        imageUrl: $('#attractionImageUrl').val().trim(),
        mrt: $('#attractionMrt').val().trim(),
        category: $('#attractionCategory').val().trim(),
        isActive: $('input[name="attractionIsActive"]:checked').val() === 'true'
    };

    if (!apiData.name) {
        $errorElement.text('行程名稱為必填欄位。');
        $saveButton.prop('disabled', false);
        return;
    }

    const url = isEditMode ? `/attractions/${attractionId}` : '/attractions';
    const method = isEditMode ? 'PUT' : 'POST';

    try {
        await ajaxWithAuth(url, {
            method: method,
            data: JSON.stringify(apiData)
        });
        showToast(`行程${isEditMode ? '更新' : '新增'}成功`, 'success');
        closeModal('attractionModal');
        loadAttractions(isEditMode ? currentAttractionsPage : 0);
    } catch (error) {
         if (!error.message || !error.message.includes('權限不足')) {
            const errorMessage = (error.responseJSON && error.responseJSON.message) ? error.responseJSON.message : `行程${isEditMode ? '更新' : '新增'}失敗`;
            $errorElement.text(errorMessage);
            showToast(`錯誤: ${errorMessage}`, 'error');
        }
    } finally {
        $saveButton.prop('disabled', false);
    }
}

async function handleToggleAttractionStatus(attractionId, currentStatus) {
    if (currentUserRole !== 'ROLE_ADMIN' && currentUserRole !== 'ROLE_TRIP_MANAGER') {
         showToast('權限不足，無法執行此操作。', 'error');
        return;
    }

    const newStatus = !currentStatus;
    const actionText = newStatus ? '啟用' : '停用';

    try {
        await ajaxWithAuth(`/attractions/${attractionId}/status`, {
            method: 'PATCH',
            data: JSON.stringify({ isActive: newStatus })
        });
        showToast(`行程 ${actionText} 成功`, 'success');
        loadAttractions(currentAttractionsPage);
    } catch (error) {
         if (!error.message || !error.message.includes('權限不足')) {
            showToast(`無法${actionText}行程: ${(error.responseJSON?.message || '請稍後再試')}`, 'error');
        }
    }
}

function confirmDeleteAttraction(attractionId, attractionName) {
    if (currentUserRole !== 'ROLE_ADMIN' && currentUserRole !== 'ROLE_TRIP_MANAGER') {
         showToast('權限不足，無法執行此操作。', 'error');
        return;
    }
    $('#deleteAttractionId').val(attractionId);
    $('#deleteAttractionName').text(attractionName || `ID: ${attractionId}`);
    openModal('deleteAttractionConfirmModal');
}

async function executeDeleteAttraction() {
    const attractionId = $('#deleteAttractionId').val();
    const $confirmButton = $('#confirmDeleteAttractionBtn');

    if (!attractionId) {
        showToast('無法獲取行程 ID', 'error');
        closeModal('deleteAttractionConfirmModal');
        return;
    }

    $confirmButton.prop('disabled', true);

    try {
        await ajaxWithAuth(`/attractions/${attractionId}`, { method: 'DELETE' });
        showToast('行程刪除成功', 'success');
        closeModal('deleteAttractionConfirmModal');
        loadAttractions();
    } catch (error) {
         if (!error.message || !error.message.includes('權限不足')) {
            showToast(`刪除行程失敗: ${(error.responseJSON?.message || '請稍後再試')}`, 'error');
        }
        closeModal('deleteAttractionConfirmModal');
    } finally {
        $confirmButton.prop('disabled', false);
    }
}


function displayFormErrors(form, errorMessage) {
    const $form = $(form);
    let $errorContainer = $form.find('p.text-red-500').first();
    if ($errorContainer.length) {
        $errorContainer.text(errorMessage);
    }
}

function clearFormErrors(form) {
    const $form = $(form);
    $form.find('p.text-red-500').text('');
    $form.find('.border-red-500').removeClass('border-red-500');
}

function showToast(message, type = 'info') {
    const $notificationArea = $('#notificationArea');
    if (!$notificationArea.length) return;

    let bgColor, textColor, borderColor, iconClass;

    switch (type) {
        case 'success':
            bgColor = 'bg-green-100'; textColor = 'text-green-800'; borderColor = 'border-green-400'; iconClass = 'fas fa-check-circle';
            break;
        case 'error':
            bgColor = 'bg-red-100'; textColor = 'text-red-800'; borderColor = 'border-red-400'; iconClass = 'fas fa-times-circle';
            break;
        default:
            bgColor = 'bg-blue-100'; textColor = 'text-blue-800'; borderColor = 'border-blue-400'; iconClass = 'fas fa-info-circle';
            break;
    }

    const $toast = $('<div>')
        .addClass(`flex items-center p-4 mb-2 rounded-md shadow-lg border-l-4 ${bgColor} ${textColor} ${borderColor} transform transition-all duration-300 ease-out opacity-0 translate-x-full`)
        .html(`
            <i class="${iconClass} mr-3 text-lg"></i>
            <span class="flex-grow">${message}</span>
            <button class="close-toast ml-4 text-xl font-semibold leading-none focus:outline-none hover:text-black">&times;</button>
        `);

    $toast.find('.close-toast').on('click', function() {
        hideToast($(this).closest('div'));
    });

    $notificationArea.append($toast);
    setTimeout(() => {
        $toast.removeClass('opacity-0 translate-x-full').addClass('opacity-100 translate-x-0');
    }, 10);

    setTimeout(() => {
        hideToast($toast);
    }, 5000);
}

function hideToast($toast) {
    if (!$toast || !$toast.length || !$toast.parent().length) return;

    $toast.removeClass('opacity-100 translate-x-0').addClass('opacity-0 translate-x-full');
    setTimeout(() => {
        $toast.remove();
    }, 300);
}

function logout() {
    localStorage.removeItem('jwtToken');
    currentUserRole = null;

    if (bookingTrendChartInstance) {
        try { bookingTrendChartInstance.destroy(); } catch (e) {}
        bookingTrendChartInstance = null;
    }
    if (userRoleChartInstance) {
        try { userRoleChartInstance.destroy(); } catch (e) {}
        userRoleChartInstance = null;
    }

    showLoginPage();
    if ($loginForm.length) {
        $loginForm.on('submit', handleLogin);
    }
    $('#dashboardContent').find('p[id]').text('載入中...');
    $('#bookChartError, #roleChartError').text('');

    const trendCtx = document.getElementById('bookingTrendChart')?.getContext('2d');
    if(trendCtx) trendCtx.clearRect(0, 0, trendCtx.canvas.width, trendCtx.canvas.height);
    const roleCtx = document.getElementById('userRoleChart')?.getContext('2d');
    if(roleCtx) roleCtx.clearRect(0, 0, roleCtx.canvas.width, roleCtx.canvas.height);

    showToast('您已成功登出', 'success');
}

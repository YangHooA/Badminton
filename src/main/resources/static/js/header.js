document.addEventListener("DOMContentLoaded", function () {
    const openBtn = document.getElementById('openCartSidebar');
    const closeBtn = document.getElementById('closeCartSidebar');
    const cartSidebar = document.getElementById('cartSidebar');
    const cartBody = cartSidebar.querySelector('.cart-body');

    function getCsrfToken() {
        const meta = document.querySelector('meta[name="_csrf"]');
        return meta ? meta.content : null;
    }

    function formatCurrency(value) {
        return value.toLocaleString('vi-VN') + 'đ';
    }

    function showLoading() {
        cartBody.innerHTML = '<div class="loading">Đang tải giỏ hàng...</div>';
    }

    function showEmptyCart() {
        cartBody.innerHTML = '<div class="empty-cart">Giỏ hàng của bạn đang trống</div>';
    }

    function saveGuestCart(items) {
        localStorage.setItem('guestCart', JSON.stringify(items));
    }

    function loadGuestCart() {
        return JSON.parse(localStorage.getItem('guestCart')) || [];
    }

    function updateTotal(container) {
        let grandTotal = 0;
        container.querySelectorAll(".cart-item").forEach((item) => {
            const quantity = parseInt(item.querySelector(".quantity").innerText);
            const price = parseInt(item.dataset.price);
            const total = quantity * price;
            item.querySelector(".item-total").innerText = formatCurrency(total);
            grandTotal += total;
        });

        const grandTotalEl = container.querySelector("#grandTotal");
        if (grandTotalEl) {
            grandTotalEl.innerText = formatCurrency(grandTotal);
        }
    }

    function attachQuantityHandlers(container) {
        container.querySelectorAll(".btn-plus").forEach((btn) => {
            btn.addEventListener("click", function () {
                const cartItem = this.closest(".cart-item");
                const quantityElem = cartItem.querySelector(".quantity");
                const productId = cartItem.dataset.productId;
                let quantity = parseInt(quantityElem.innerText);
                quantity++;
                quantityElem.innerText = quantity;
                updateTotal(container);
                updateCartItem(productId, quantity);
            });
        });

        container.querySelectorAll(".btn-minus").forEach((btn) => {
            btn.addEventListener("click", function () {
                const cartItem = this.closest(".cart-item");
                const quantityElem = cartItem.querySelector(".quantity");
                const productId = cartItem.dataset.productId;
                let quantity = parseInt(quantityElem.innerText);
                if (quantity > 1) {
                    quantity--;
                    quantityElem.innerText = quantity;
                    updateTotal(container);
                    updateCartItem(productId, quantity);
                }
            });
        });
    }

    async function validateToken(token) {
        try {
            const response = await fetch('/api/validate-token', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ token: token }),
                credentials: 'include'
            });
            const result = await response.json();
            return result.valid;
        } catch (error) {
            console.error('Lỗi khi kiểm tra token:', error);
            return false;
        }
    }

    async function getAuthInfo() {
        try {
            const response = await fetch('/check-auth', { credentials: 'include' });
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();
            if (data.authenticated && data.user?.email && data.token) {
                const isTokenValid = await validateToken(data.token);
                if (isTokenValid) {
                    return { email: data.user.email, token: data.token };
                } else {
                    Toastify({
                        text: 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.',
                        duration: 3000,
                        close: true,
                        gravity: 'top',
                        position: 'right',
                        backgroundColor: '#f44336',
                        stopOnFocus: true
                    }).showToast();
                    return null;
                }
            }
            return null;
        } catch (error) {
            console.error('Lỗi lấy thông tin xác thực:', error);
            Toastify({
                text: 'Vui lòng đăng nhập để tiếp tục',
                duration: 3000,
                close: true,
                gravity: 'top',
                position: 'right',
                backgroundColor: '#f44336',
                stopOnFocus: true
            }).showToast();
            return null;
        }
    }

    async function updateCartItem(productId, quantity) {
        const authInfo = await getAuthInfo();
        if (!authInfo) {
            let guestCart = loadGuestCart();
            const existingItem = guestCart.find(item => item.productId === parseInt(productId));
            if (existingItem) {
                existingItem.quantity = quantity;
            } else {
                guestCart.push({ productId: parseInt(productId), quantity });
            }
            saveGuestCart(guestCart);
            Toastify({
                text: 'Đã cập nhật giỏ hàng tạm thời',
                duration: 3000,
                backgroundColor: '#4CAF50'
            }).showToast();
            return;
        }

        fetch('/api/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': getCsrfToken(),
                'Authorization': `Bearer ${authInfo.token}`
            },
            body: JSON.stringify({
                productId: parseInt(productId),
                quantity: quantity,
                email: authInfo.email
            }),
            credentials: 'include'
        })
            .then(response => response.json())
            .then(data => {
                Toastify({
                    text: data.message,
                    duration: 3000,
                    close: true,
                    gravity: 'top',
                    position: 'right',
                    backgroundColor: data.success ? '#4CAF50' : '#f44336',
                    stopOnFocus: true
                }).showToast();
            })
            .catch(error => {
                console.error('Lỗi khi cập nhật số lượng:', error);
                Toastify({
                    text: 'Lỗi khi cập nhật số lượng',
                    duration: 3000,
                    close: true,
                    gravity: 'top',
                    position: 'right',
                    backgroundColor: '#f44336',
                    stopOnFocus: true
                }).showToast();
            });
    }

    function displayCartItems(items) {
        const cartItemsContainer = document.createElement('div');
        cartItemsContainer.className = 'cart-items';
        cartItemsContainer.innerHTML = '';

        if (items.length === 0) {
            showEmptyCart();
            return;
        }

        items.forEach(item => {
            const cartItem = document.createElement('div');
            cartItem.className = 'cart-item d-flex align-items-center mb-3';
            cartItem.dataset.price = item.price;
            cartItem.dataset.productId = item.productId;
            cartItem.innerHTML = `
                <img src="${item.image || 'default-image.jpg'}" alt="${item.name || 'Sản phẩm'}" class="me-3 rounded border" width="150">
                <div class="flex-grow-1">
                    <div class="fw-bold">${item.name || 'Sản phẩm không xác định'}</div>
                    <div class="text-muted small">${item.description || ''}</div>
                    <div class="d-flex align-items-center mt-2">
                        <button class="btn btn-outline-secondary btn-sm me-1 btn-minus">−</button>
                        <span class="px-2 quantity">${item.quantity || 1}</span>
                        <button class="btn btn-outline-secondary btn-sm ms-1 btn-plus">+</button>
                        <button class="btn btn-outline-danger btn-sm ms-2 btn-remove" title="Xóa sản phẩm">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </div>
                <div class="text-end ms-2 fw-bold text-danger item-total">${formatCurrency((item.price || 0) * (item.quantity || 1))}</div>
            `;
            cartItemsContainer.appendChild(cartItem);
        });

        cartBody.innerHTML = '';
        cartItemsContainer.appendChild(document.createElement('hr'));
        cartItemsContainer.appendChild(cartBody.querySelector('.d-flex.justify-content-between.fw-bold').parentElement);
        cartBody.appendChild(cartItemsContainer);

        cartItemsContainer.querySelectorAll('.btn-remove').forEach(btn => {
            btn.addEventListener('click', function () {
                const cartItem = this.closest('.cart-item');
                const productId = cartItem.dataset.productId;
                removeCartItem(productId, cartItem);
            });
        });

        attachQuantityHandlers(cartSidebar);
        updateTotal(cartSidebar);
    }

    async function removeCartItem(productId, cartItemElement) {
        const authInfo = await getAuthInfo();
        if (!authInfo) {
            let guestCart = loadGuestCart();
            guestCart = guestCart.filter(item => item.productId !== parseInt(productId));
            saveGuestCart(guestCart);
            cartItemElement.remove();
            updateTotal(cartSidebar);
            Toastify({
                text: 'Xóa sản phẩm thành công',
                duration: 3000,
                backgroundColor: '#4CAF50'
            }).showToast();
            if (!cartBody.querySelector('.cart-item')) {
                showEmptyCart();
            }
            const badge = document.querySelector('.cart-icon .badge');
            badge.textContent = guestCart.length;
            return;
        }

        Swal.fire({
            title: 'Xác nhận xóa',
            text: 'Bạn có muốn xóa sản phẩm này khỏi giỏ hàng?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Xóa',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                fetch(`/api/cart/remove`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': getCsrfToken(),
                        'Authorization': `Bearer ${authInfo.token}`
                    },
                    body: JSON.stringify({ productId: parseInt(productId) }),
                    credentials: 'include'
                })
                    .then(response => response.json())
                    .then(data => {
                        if (data.success) {
                            cartItemElement.remove();
                            updateTotal(cartSidebar);
                            Toastify({
                                text: data.message,
                                duration: 3000,
                                close: true,
                                gravity: 'top',
                                position: 'right',
                                backgroundColor: '#4CAF50',
                                stopOnFocus: true
                            }).showToast();
                            const badge = document.querySelector('.cart-icon .badge');
                            badge.textContent = data.itemCount || 0;
                            if (!cartBody.querySelector('.cart-item')) {
                                showEmptyCart();
                            }
                        } else {
                            Toastify({
                                text: data.message,
                                duration: 3000,
                                close: true,
                                gravity: 'top',
                                position: 'right',
                                backgroundColor: '#f44336',
                                stopOnFocus: true
                            }).showToast();
                        }
                    })
                    .catch(error => {
                        console.error('Lỗi khi xóa sản phẩm:', error);
                        Toastify({
                            text: 'Lỗi khi xóa sản phẩm',
                            duration: 3000,
                            close: true,
                            gravity: 'top',
                            position: 'right',
                            backgroundColor: '#f44336',
                            stopOnFocus: true
                        }).showToast();
                    });
            }
        });
    }

    async function syncGuestCart(email, token) {
        const guestCart = loadGuestCart();
        if (guestCart.length === 0) return;

        for (const item of guestCart) {
            await fetch('/api/cart/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken(),
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    productId: item.productId,
                    quantity: item.quantity,
                    email: email
                }),
                credentials: 'include'
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        Toastify({
                            text: `Đã đồng bộ sản phẩm ${item.productId}`,
                            duration: 2000,
                            backgroundColor: '#4CAF50'
                        }).showToast();
                    } else {
                        Toastify({
                            text: data.message,
                            duration: 3000,
                            backgroundColor: '#f44336'
                        }).showToast();
                    }
                });
        }
        localStorage.removeItem('guestCart');
    }

    fetch('/check-auth', {
        credentials: 'include'
    })
        .then(response => response.json())
        .then(data => {
            if (data.authenticated && data.user?.email && data.token) {
                syncGuestCart(data.user.email, data.token);
                updateAuthUI(data);
            } else {
                updateAuthUI({ authenticated: false });
            }
        })
        .catch(error => console.error('Lỗi khi kiểm tra đăng nhập:', error));

    if (openBtn && cartSidebar) {
        openBtn.addEventListener('click', async () => {
            cartSidebar.classList.add('active');
            showLoading();

            const authInfo = await getAuthInfo();
            if (!authInfo) {
                const guestCart = loadGuestCart();
                displayCartItems(guestCart);
                const badge = document.querySelector('.cart-icon .badge');
                badge.textContent = guestCart.length;
                return;
            }

            fetch('/api/cart/items', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${authInfo.token}`
                },
                credentials: 'include'
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP error! Status: ${response.status}`);
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.success) {
                        displayCartItems(data.items);
                        const badge = document.querySelector('.cart-icon .badge');
                        badge.textContent = data.items.length;
                    } else {
                        showEmptyCart();
                        const badge = document.querySelector('.cart-icon .badge');
                        badge.textContent = '0';
                        Toastify({
                            text: data.message,
                            duration: 3000,
                            close: true,
                            gravity: 'top',
                            position: 'right',
                            backgroundColor: '#f44336',
                            stopOnFocus: true
                        }).showToast();
                    }
                })
                .catch(error => {
                    console.error('Lỗi khi lấy giỏ hàng:', error);
                    showEmptyCart();
                    Toastify({
                        text: 'Lỗi khi tải giỏ hàng: ' + error.message,
                        duration: 3000,
                        close: true,
                        gravity: 'top',
                        position: 'right',
                        backgroundColor: '#f44336',
                        stopOnFocus: true
                    }).showToast();
                });
        });
    }

    if (closeBtn && cartSidebar) {
        closeBtn.addEventListener('click', () => {
            cartSidebar.classList.remove('active');
        });
    }

    window.addEventListener('click', (e) => {
        if (cartSidebar && openBtn && !cartSidebar.contains(e.target) && !openBtn.contains(e.target)) {
            cartSidebar.classList.remove('active');
        }
    });

    function updateAuthUI(data) {
        const loginNavItem = document.getElementById('loginNavItem');
        const loginLink = document.getElementById('loginLink');
        const navbarNav = loginNavItem.parentElement;

        const existingLogout = navbarNav.querySelector('.nav-item.logout');
        if (existingLogout) {
            existingLogout.remove();
        }

        if (data.authenticated) {
            loginLink.innerHTML = `<i class="bi bi-person"></i> ${data.user.name}`;
            loginLink.href = "#";
            loginLink.removeAttribute('data-bs-target');
            loginLink.removeAttribute('data-bs-toggle');

            const logoutItem = document.createElement('li');
            logoutItem.className = 'nav-item logout';
            logoutItem.innerHTML = '<a class="nav-link" href="#" id="logoutLink">Đăng Xuất</a>';
            navbarNav.appendChild(logoutItem);

            document.getElementById('logoutLink').addEventListener('click', handleLogout);
        } else {
            loginLink.innerHTML = 'Đăng Nhập';
            loginLink.href = "#";
            loginLink.setAttribute('data-bs-toggle', 'modal');
            loginLink.setAttribute('data-bs-target', '#loginModal');
        }
    }

    function handleLogout(event) {
        event.preventDefault();

        Swal.fire({
            title: 'Xác nhận đăng xuất',
            text: 'Bạn có chắc chắn muốn đăng xuất?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Đăng xuất',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                fetch('/logout', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': getCsrfToken()
                    },
                    credentials: 'include'
                })
                    .then(response => {
                        if (response.ok) {
                            Toastify({
                                text: 'Đăng xuất thành công!',
                                duration: 3000,
                                close: true,
                                gravity: 'top',
                                position: 'right',
                                backgroundColor: '#4CAF50',
                                stopOnFocus: true
                            }).showToast();
                            updateAuthUI({ authenticated: false });
                            setTimeout(() => {
                                window.location.href = '/';
                            }, 1000);
                        } else {
                            throw new Error('Đăng xuất thất bại');
                        }
                    })
                    .catch(error => {
                        console.error('Lỗi khi đăng xuất:', error);
                        Toastify({
                            text: 'Đăng xuất thất bại. Vui lòng thử lại!',
                            duration: 3000,
                            close: true,
                            gravity: 'top',
                            position: 'right',
                            backgroundColor: '#f44336',
                            stopOnFocus: true
                        }).showToast();
                    });
            }
        });
    }
});
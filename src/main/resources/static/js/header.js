document.addEventListener("DOMContentLoaded", function () {
    // Các biến và hàm tiện ích
    const openBtn = document.getElementById('openCartSidebar');
    const closeBtn = document.getElementById('closeCartSidebar');
    const cartSidebar = document.getElementById('cartSidebar');
    const cartBody = cartSidebar?.querySelector('.cart-body');

    function getCsrfToken() {
        const meta = document.querySelector('meta[name="_csrf"]');
        return meta ? meta.content : null;
    }

    function formatCurrency(value) {
        return value.toLocaleString('vi-VN') + 'đ';
    }

    function showLoading() {
        if (cartBody) {
            cartBody.innerHTML = '<div class="loading text-center">Đang tải giỏ hàng...</div>';
        }
    }

    function showEmptyCart() {
        if (cartBody) {
            cartBody.innerHTML = '<div class="empty-cart text-center">Giỏ hàng của bạn đang trống</div>';
        }
    }

    function saveGuestCart(items) {
        localStorage.setItem('guestCart', JSON.stringify(items));
    }

    function loadGuestCart() {
        return JSON.parse(localStorage.getItem('guestCart')) || [];
    }

    function updateBadge(count) {
        const badge = document.querySelector('.cart-icon .badge');
        if (badge) {
            badge.textContent = count;
            badge.style.display = count > 0 ? 'block' : 'none';
        }
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

    // Hàm kiểm tra token
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

    // Hàm lấy thông tin xác thực
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
                    return { email: data.user.email, token: data.token, name: data.user.name };
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
                    setTimeout(() => window.location.href = '/login', 1500);
                    return null;
                }
            }
            return null;
        } catch (error) {
            console.error('Lỗi lấy thông tin xác thực:', error);
            return null;
        }
    }

    // Hàm cập nhật giỏ hàng
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
            updateBadge(guestCart.length);
            Toastify({
                text: 'Đã cập nhật giỏ hàng tạm thời',
                duration: 3000,
                backgroundColor: '#4CAF50'
            }).showToast();
            return;
        }

        try {
            const response = await fetch('/api/cart/add', {
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
            });
            const data = await response.json();
            Toastify({
                text: data.message,
                duration: 3000,
                close: true,
                gravity: 'top',
                position: 'right',
                backgroundColor: data.success ? '#4CAF50' : '#f44336',
                stopOnFocus: true
            }).showToast();
        } catch (error) {
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
        }
    }

    // Hàm xóa sản phẩm khỏi giỏ hàng
    async function removeCartItem(productId, cartItemElement) {
        const authInfo = await getAuthInfo();
        if (!authInfo) {
            let guestCart = loadGuestCart();
            guestCart = guestCart.filter(item => item.productId !== parseInt(productId));
            saveGuestCart(guestCart);
            cartItemElement.remove();
            updateTotal(cartSidebar);
            updateBadge(guestCart.length);
            syncModalCartItems(guestCart);
            Toastify({
                text: 'Xóa sản phẩm thành công',
                duration: 3000,
                backgroundColor: '#4CAF50'
            }).showToast();
            if (!cartBody.querySelector('.cart-item')) {
                showEmptyCart();
            }
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
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    const response = await fetch(`/api/cart/remove`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'X-CSRF-TOKEN': getCsrfToken(),
                            'Authorization': `Bearer ${authInfo.token}`
                        },
                        body: JSON.stringify({ productId: parseInt(productId) }),
                        credentials: 'include'
                    });
                    const data = await response.json();
                    if (data.success) {
                        cartItemElement.remove();
                        updateTotal(cartSidebar);
                        updateBadge(data.itemCount || 0);
                        const currentItems = await fetchCartItems(authInfo.token);
                        syncModalCartItems(currentItems);
                        Toastify({
                            text: data.message,
                            duration: 3000,
                            close: true,
                            gravity: 'top',
                            position: 'right',
                            backgroundColor: '#4CAF50',
                            stopOnFocus: true
                        }).showToast();
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
                } catch (error) {
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
                }
            }
        });
    }

    // Hàm lấy danh sách sản phẩm trong giỏ hàng
    async function fetchCartItems(token) {
        try {
            const response = await fetch('/api/cart/items', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                credentials: 'include'
            });
            const data = await response.json();
            return data.success ? data.items : [];
        } catch (error) {
            console.error('Lỗi khi lấy giỏ hàng:', error);
            return [];
        }
    }

    // Hàm hiển thị sản phẩm trong giỏ hàng
    function displayCartItems(items, container) {
        const cartItemsContainer = document.createElement('div');
        cartItemsContainer.className = 'cart-items';
        cartItemsContainer.innerHTML = '';

        if (items.length === 0) {
            container.innerHTML = '<div class="empty-cart text-center">Giỏ hàng của bạn đang trống</div>';
            updateBadge(0);
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

        container.innerHTML = '';
        cartItemsContainer.appendChild(document.createElement('hr'));
        const totalDiv = document.createElement('div');
        totalDiv.className = 'd-flex justify-content-between fw-bold';
        totalDiv.innerHTML = `<span>Tổng cộng</span><span class="text-danger" id="grandTotal">${formatCurrency(0)}</span>`;
        cartItemsContainer.appendChild(totalDiv);
        container.appendChild(cartItemsContainer);

        cartItemsContainer.querySelectorAll('.btn-remove').forEach(btn => {
            btn.addEventListener('click', function () {
                const cartItem = this.closest('.cart-item');
                const productId = cartItem.dataset.productId;
                removeCartItem(productId, cartItem);
            });
        });

        attachQuantityHandlers(container);
        updateTotal(container);
        updateBadge(items.length);
    }

    // Hàm đồng bộ giỏ hàng trong modal
    function syncModalCartItems(items) {
        const modalCartBody = document.querySelector('#cartModal .modal-body .col-md-8');
        if (!modalCartBody) return;

        modalCartBody.innerHTML = items.length === 0
            ? '<div class="empty-cart text-center">Giỏ hàng của bạn đang trống</div>'
            : '';

        items.forEach(item => {
            const cartItem = document.createElement('div');
            cartItem.className = 'cart-item d-flex align-items-center mb-3';
            cartItem.dataset.price = item.price;
            cartItem.dataset.productId = item.productId;
            cartItem.innerHTML = `
                <img src="${item.image || 'default-image.jpg'}" class="me-3 rounded border" width="150" />
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
            modalCartBody.appendChild(cartItem);
        });

        modalCartBody.querySelectorAll('.btn-remove').forEach(btn => {
            btn.addEventListener('click', function () {
                const cartItem = this.closest('.cart-item');
                const productId = cartItem.dataset.productId;
                removeCartItem(productId, cartItem);
            });
        });

        attachQuantityHandlers(modalCartBody);
        updateTotal(modalCartBody);
        updateModalSummary(items);
    }

    // Hàm cập nhật tóm tắt đơn hàng trong modal
    function updateModalSummary(items) {
        const subtotalEl = document.querySelector('#cartModal #subtotal');
        const grandTotalEl = document.querySelector('#cartModal #grandTotalView');
        if (!subtotalEl || !grandTotalEl) return;

        let grandTotal = 0;
        items.forEach(item => {
            grandTotal += (item.price || 0) * (item.quantity || 1);
        });

        subtotalEl.textContent = formatCurrency(grandTotal);
        grandTotalEl.textContent = formatCurrency(grandTotal);
    }

    // Hàm đồng bộ giỏ hàng khách với tài khoản
    async function syncGuestCart(email, token) {
        const guestCart = loadGuestCart();
        if (guestCart.length === 0) return;

        try {
            const response = await fetch('/api/cart/sync-guest-cart', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken(),
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(guestCart),
                credentials: 'include'
            });
            const data = await response.json();
            if (data.success) {
                localStorage.removeItem('guestCart');
                Toastify({
                    text: 'Đã đồng bộ giỏ hàng',
                    duration: 3000,
                    backgroundColor: '#4CAF50'
                }).showToast();
            } else {
                Toastify({
                    text: data.message,
                    duration: 3000,
                    backgroundColor: '#f44336'
                }).showToast();
            }
        } catch (error) {
            console.error('Lỗi khi đồng bộ giỏ hàng:', error);
            Toastify({
                text: 'Lỗi khi đồng bộ giỏ hàng',
                duration: 3000,
                backgroundColor: '#f44336'
            }).showToast();
        }
    }

    // Xử lý sự kiện mở sidebar giỏ hàng
    if (openBtn && cartSidebar) {
        openBtn.addEventListener('click', async () => {
            cartSidebar.classList.add('active');
            showLoading();

            const authInfo = await getAuthInfo();
            if (!authInfo || !authInfo.token) {
                const guestCart = loadGuestCart();
                displayCartItems(guestCart, cartBody);
                syncModalCartItems(guestCart);
                return;
            }

            const items = await fetchCartItems(authInfo.token);
            displayCartItems(items, cartBody);
            syncModalCartItems(items);
        });
    }

    // Xử lý sự kiện đóng sidebar
    if (closeBtn && cartSidebar) {
        closeBtn.addEventListener('click', () => {
            cartSidebar.classList.remove('active');
        });
    }

    // Đóng sidebar khi nhấp ra ngoài
    window.addEventListener('click', (e) => {
        if (cartSidebar && openBtn && !cartSidebar.contains(e.target) && !openBtn.contains(e.target)) {
            cartSidebar.classList.remove('active');
        }
    });

    // Hàm lấy danh sách tỉnh/thành phố từ API
    async function fetchProvinces() {
        try {
            const response = await fetch('https://provinces.open-api.vn/api/p/');
            if (!response.ok) {
                throw new Error('Không thể lấy danh sách tỉnh/thành phố');
            }
            const provinces = await response.json();
            populateProvinces(provinces);
        } catch (error) {
            console.error('Lỗi khi lấy danh sách tỉnh/thành phố:', error);
            Swal.fire({
                icon: 'error',
                title: 'Lỗi',
                text: 'Không thể tải danh sách tỉnh/thành phố.',
            });
        }
    }

    // Hàm điền danh sách tỉnh/thành phố vào select
    function populateProvinces(provinces) {
        const provinceSelect = document.getElementById('provinceSelect');
        if (!provinceSelect) return;

        provinceSelect.innerHTML = '<option value="">Chọn tỉnh/thành</option>';
        provinces.forEach(province => {
            const option = document.createElement('option');
            option.value = province.code;
            option.textContent = province.name;
            provinceSelect.appendChild(option);
        });
    }

    // Hàm lấy danh sách quận/huyện từ API
    async function fetchDistricts(provinceCode) {
        try {
            const response = await fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`);
            if (!response.ok) {
                throw new Error('Không thể lấy danh sách quận/huyện');
            }
            const data = await response.json();
            populateDistricts(data.districts);
        } catch (error) {
            console.error('Lỗi khi lấy danh sách quận/huyện:', error);
            Swal.fire({
                icon: 'error',
                title: 'Lỗi',
                text: 'Không thể tải danh sách quận/huyện.',
            });
        }
    }

    // Hàm điền danh sách quận/huyện vào select
    function populateDistricts(districts) {
        const districtSelect = document.getElementById('districtSelect');
        if (!districtSelect) return;

        districtSelect.innerHTML = '<option value="">Chọn quận/huyện</option>';
        districtSelect.disabled = false;

        districts.forEach(district => {
            const option = document.createElement('option');
            option.value = district.code;
            option.textContent = district.name;
            districtSelect.appendChild(option);
        });
    }

    // Xử lý sự kiện khi chọn tỉnh/thành phố
    const provinceSelect = document.getElementById('provinceSelect');
    if (provinceSelect) {
        provinceSelect.addEventListener('change', function () {
            const provinceCode = this.value;
            const districtSelect = document.getElementById('districtSelect');

            if (provinceCode) {
                fetchDistricts(provinceCode);
            } else {
                districtSelect.innerHTML = '<option value="">Chọn quận/huyện</option>';
                districtSelect.disabled = true;
            }
        });
    }

    // Gọi hàm lấy danh sách tỉnh/thành phố khi modal thanh toán mở
    const checkoutModal = document.getElementById('checkoutModal');
    if (checkoutModal) {
        checkoutModal.addEventListener('shown.bs.modal', function () {
            fetchProvinces();
        });
    }

    // Hàm lấy danh sách sản phẩm trong giỏ hàng và hiển thị trong modal thanh toán
    function loadOrderItems() {
        fetch('/api/cart/items', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': getJwtToken() ? `Bearer ${getJwtToken()}` : ''
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const orderItemsContainer = document.getElementById('orderItems');
                    const itemCountElement = document.getElementById('itemCount');
                    const orderTotalElement = document.getElementById('orderTotal');
                    orderItemsContainer.innerHTML = '';

                    let totalPrice = 0;
                    let itemCount = data.items.length;

                    if (itemCount === 0) {
                        orderItemsContainer.innerHTML = '<p class="text-white-50">Giỏ hàng của bạn đang trống.</p>';
                        itemCountElement.textContent = '0';
                        orderTotalElement.textContent = '0đ';
                        return;
                    }

                    data.items.forEach(item => {
                        const itemTotal = item.price * item.quantity;
                        totalPrice += itemTotal;

                        const itemHtml = `
                            <div class="mb-3">
                                <div class="fw-bold">${item.name}</div>
                                <div class="text-white-50 small">${item.quantity} x ${formatCurrency(item.price)}</div>
                                <div class="text-white-50 small">${item.description ? `Chọn: ${item.description}` : ''}</div>
                            </div>
                        `;
                        orderItemsContainer.innerHTML += itemHtml;
                    });

                    itemCountElement.textContent = itemCount;
                    orderTotalElement.textContent = formatCurrency(totalPrice);
                } else {
                    console.error('Lỗi khi lấy giỏ hàng:', data.message);
                    Swal.fire({
                        icon: 'error',
                        title: 'Lỗi',
                        text: data.message || 'Không thể tải giỏ hàng.',
                    });
                }
            })
            .catch(error => {
                console.error('Lỗi khi gọi API giỏ hàng:', error);
                Swal.fire({
                    icon: 'error',
                    title: 'Lỗi',
                    text: 'Có lỗi xảy ra khi tải giỏ hàng.',
                });
            });
    }

    // Hàm lấy JWT token từ cookie
    function getJwtToken() {
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            const [name, value] = cookie.trim().split('=');
            if (name === 'jwtToken') {
                return value;
            }
        }
        return null;
    }

    // Gọi loadOrderItems khi modal thanh toán mở
    if (checkoutModal) {
        checkoutModal.addEventListener('shown.bs.modal', function () {
            loadOrderItems();
        });
    }

    // Xử lý nút "Đặt hàng"
    const placeOrderBtn = document.getElementById('placeOrderBtn');
    if (placeOrderBtn) {
        placeOrderBtn.addEventListener('click', async function () {
            const form = document.querySelector('#checkoutModal form');
            if (!form.checkValidity()) {
                form.reportValidity();
                return;
            }

            const firstName = form.querySelector('input[placeholder="Nhập tên của bạn"]').value;
            const lastName = form.querySelector('input[placeholder="Nhập họ của bạn"]').value;
            const guestName = `${firstName} ${lastName}`.trim();
            const guestEmail = form.querySelector('input[placeholder="Nhập Email"]').value;
            const guestPhone = form.querySelector('input[placeholder="Nhập số điện thoại"]').value;
            const province = document.getElementById('provinceSelect').selectedOptions[0]?.text;
            const district = document.getElementById('districtSelect').selectedOptions[0]?.text;
            const street = form.querySelector('input[placeholder="House number and street name"]').value;
            const guestAddress = `${street}, ${district}, ${province}`.trim();
            const paymentMethod = document.getElementById('btnCOD').classList.contains('active') ? 'COD' : 'ONLINE';

            try {
                const response = await fetch('/api/cart/place-order', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': getCsrfToken(),
                        'Authorization': getJwtToken() ? `Bearer ${getJwtToken()}` : ''
                    },
                    body: JSON.stringify({
                        guestName,
                        guestEmail,
                        guestPhone,
                        guestAddress,
                        paymentMethod
                    }),
                    credentials: 'include'
                });

                const data = await response.json();
                if (data.success) {
                    Swal.fire({
                        icon: 'success',
                        title: 'Thành công',
                        text: `Đơn hàng của bạn đã được đặt! Mã đơn hàng: ${data.orderId}`,
                    }).then(() => {
                        bootstrap.Modal.getInstance(document.getElementById('checkoutModal')).hide();
                        window.location.href = '/';
                    });
                    // Cập nhật badge giỏ hàng
                    updateBadge(0);
                    // Xóa giỏ hàng hiển thị
                    if (cartBody) {
                        showEmptyCart();
                    }
                } else {
                    Swal.fire({
                        icon: 'error',
                        title: 'Lỗi',
                        text: data.message || 'Không thể đặt hàng. Vui lòng thử lại.',
                    });
                }
            } catch (error) {
                console.error('Lỗi khi đặt hàng:', error);
                Swal.fire({
                    icon: 'error',
                    title: 'Lỗi',
                    text: 'Có lỗi xảy ra khi đặt hàng. Vui lòng thử lại.',
                });
            }
        });
    }

    // Xử lý chuyển đổi phương thức thanh toán
    const btnCOD = document.getElementById('btnCOD');
    const btnOnline = document.getElementById('btnOnline');

    if (btnCOD && btnOnline) {
        btnCOD.addEventListener('click', function () {
            btnCOD.classList.add('active');
            btnOnline.classList.remove('active');
        });

        btnOnline.addEventListener('click', function () {
            btnOnline.classList.add('active');
            btnCOD.classList.remove('active');
        });
    }

    // Kiểm tra trạng thái đăng nhập và đồng bộ giỏ hàng
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

    // Hàm cập nhật giao diện xác thực
    function updateAuthUI(data) {
        const loginNavItem = document.getElementById('loginNavItem');
        const loginLink = document.getElementById('loginLink');
        const navbarNav = loginNavItem?.parentElement;

        if (!navbarNav) return;

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

    // Hàm xử lý đăng xuất
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
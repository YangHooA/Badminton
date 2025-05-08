/*btn Trang Chủ*/
document.addEventListener("DOMContentLoaded", function () {
    const homeLink = document.querySelector(".nav-link[href='/']");

    if (homeLink) {
        homeLink.addEventListener("click", function (event) {
            if (window.location.pathname === "/") {
                event.preventDefault();
                window.scrollTo({ top: 0, behavior: "smooth" });
            }
        });
    }
});

/*js form đăng nhập, đăng ký và đăng xuất*/
document.addEventListener("DOMContentLoaded", function () {
    // Hàm lấy CSRF token
    function getCsrfToken() {
        const meta = document.querySelector('meta[name="_csrf"]');
        return meta ? meta.content : null;
    }

    // Hàm cập nhật giao diện dựa trên trạng thái đăng nhập
    function updateAuthUI(data) {
        const loginNavItem = document.getElementById('loginNavItem');
        const loginLink = document.getElementById('loginLink');
        const navbarNav = loginNavItem.parentElement;

        // Xóa nút đăng xuất cũ (nếu có)
        const existingLogout = navbarNav.querySelector('.nav-item.logout');
        if (existingLogout) {
            existingLogout.remove();
        }

        if (data.authenticated) {
            // Cập nhật giao diện khi đã đăng nhập
            loginLink.innerHTML = `<i class="bi bi-person"></i> ${data.user.name}`;
            loginLink.href = "#";
            loginLink.removeAttribute('data-bs-target');
            loginLink.removeAttribute('data-bs-toggle');

            // Thêm nút đăng xuất
            const logoutItem = document.createElement('li');
            logoutItem.className = 'nav-item logout';
            logoutItem.innerHTML = '<a class="nav-link" href="#" id="logoutLink">Đăng Xuất</a>';
            navbarNav.appendChild(logoutItem);

            // Gắn sự kiện đăng xuất
            document.getElementById('logoutLink').addEventListener('click', handleLogout);
        } else {
            // Hiển thị nút đăng nhập nếu chưa đăng nhập
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

                            // Cập nhật giao diện về trạng thái chưa đăng nhập
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

    // Kiểm tra trạng thái đăng nhập
    fetch('/check-auth', {
        credentials: 'include'
    })
        .then(response => response.json())
        .then(data => updateAuthUI(data))
        .catch(error => console.error('Lỗi khi kiểm tra đăng nhập:', error));

    // Xử lý chuyển đổi form đăng nhập/đăng ký
    document.getElementById("switchToRegister").addEventListener("click", function () {
        document.getElementById("loginForm").style.display = "none";
        document.getElementById("registerForm").style.display = "block";
        document.getElementById("loginModalLabel").innerText = "Đăng Ký";
    });

    document.getElementById("switchToLogin").addEventListener("click", function () {
        document.getElementById("registerForm").style.display = "none";
        document.getElementById("loginForm").style.display = "block";
        document.getElementById("loginModalLabel").innerText = "Đăng Nhập";
    });
});

/*js cuộn Sản Phẩm*/
document.addEventListener("DOMContentLoaded", function () {
    const productLink = document.querySelector('a[href="#product-section"]');
    if (productLink) {
        productLink.addEventListener("click", function (event) {
            event.preventDefault();
            document.querySelector("#product-section").scrollIntoView({ behavior: "smooth" });
        });
    }
});

/*pop up cart*/
const openBtn = document.getElementById('openCartSidebar');
const closeBtn = document.getElementById('closeCartSidebar');
const cartSidebar = document.getElementById('cartSidebar');

if (openBtn && cartSidebar) {
    openBtn.addEventListener('click', () => {
        cartSidebar.classList.add('active');
    });
}

if (closeBtn && cartSidebar) {
    closeBtn.addEventListener('click', () => {
        cartSidebar.classList.remove('active');
    });
}

// Đóng khi nhấn ngoài
window.addEventListener('click', (e) => {
    if (cartSidebar && openBtn && !cartSidebar.contains(e.target) && !openBtn.contains(e.target)) {
        cartSidebar.classList.remove('active');
    }
});

function formatCurrency(value) {
    return value.toLocaleString('vi-VN') + 'đ';
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

    const grandTotalEl = container.querySelector("#grandTotal") || container.querySelector("#grandTotalView");
    const subtotalEl = container.querySelector("#subtotal");

    if (grandTotalEl) grandTotalEl.innerText = formatCurrency(grandTotal);
    if (subtotalEl) subtotalEl.innerText = formatCurrency(grandTotal);
}

function attachQuantityHandlers(container) {
    container.querySelectorAll(".btn-plus").forEach((btn) => {
        btn.addEventListener("click", function () {
            const quantityElem = this.closest(".cart-item").querySelector(".quantity");
            let quantity = parseInt(quantityElem.innerText);
            quantity++;
            quantityElem.innerText = quantity;
            updateTotal(container);
        });
    });

    container.querySelectorAll(".btn-minus").forEach((btn) => {
        btn.addEventListener("click", function () {
            const quantityElem = this.closest(".cart-item").querySelector(".quantity");
            let quantity = parseInt(quantityElem.innerText);
            if (quantity > 1) {
                quantity--;
                quantityElem.innerText = quantity;
                updateTotal(container);
            }
        });
    });
}

// Khởi tạo giỏ hàng
const sidebarCart = document.getElementById("cartSidebar");
const modalCart = document.getElementById("cartModal");

if (sidebarCart) {
    attachQuantityHandlers(sidebarCart);
    updateTotal(sidebarCart);
}

if (modalCart) {
    attachQuantityHandlers(modalCart);
    updateTotal(modalCart);
}

/*phương thức thanh toán*/
const btnCOD = document.getElementById("btnCOD");
const btnOnline = document.getElementById("btnOnline");

if (btnCOD && btnOnline) {
    btnCOD.addEventListener("click", () => {
        btnCOD.classList.add("active");
        btnOnline.classList.remove("active");
    });

    btnOnline.addEventListener("click", () => {
        btnOnline.classList.add("active");
        btnCOD.classList.remove("active");
    });
}

/*api hành chính VN chọn quận huyện*/
const provinceSelect = document.getElementById("provinceSelect");
const districtSelect = document.getElementById("districtSelect");

if (provinceSelect && districtSelect) {
    // Gọi API để lấy danh sách tỉnh
    fetch("https://provinces.open-api.vn/api/p/")
        .then(res => res.json())
        .then(data => {
            data.forEach(province => {
                const opt = document.createElement("option");
                opt.value = province.code;
                opt.textContent = province.name;
                provinceSelect.appendChild(opt);
            });
        });

    // Khi chọn tỉnh, gọi API để lấy quận/huyện
    provinceSelect.addEventListener("change", function () {
        const provinceCode = this.value;
        districtSelect.innerHTML = '<option value="">Chọn quận/huyện</option>';
        districtSelect.disabled = true;

        if (provinceCode) {
            fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`)
                .then(res => res.json())
                .then(data => {
                    data.districts.forEach(district => {
                        const opt = document.createElement("option");
                        opt.value = district.code;
                        opt.textContent = district.name;
                        districtSelect.appendChild(opt);
                    });
                    districtSelect.disabled = false;
                });
        }
    });

}

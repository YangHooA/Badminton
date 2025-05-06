/*btn Trang Chủ*/
document.addEventListener("DOMContentLoaded", function () {
    const homeLink = document.querySelector(".nav-link[href='/']");

    if (homeLink) {
        homeLink.addEventListener("click", function (event) {
            if (window.location.pathname === "/") {
                // Nếu đã ở trang chủ thì chỉ cuộn lên đầu
                event.preventDefault();
                window.scrollTo({top: 0, behavior: "smooth"});
            }
            // Nếu không ở trang chủ, thì để trình duyệt chuyển về "/"
        });
    }
});


/*js form đang nhập, đăng kí*/
document.addEventListener("DOMContentLoaded", function () {
    // Kiểm tra trạng thái đăng nhập
    fetch('/check-auth', {
        credentials: 'include' // Gửi cookie
    })
        .then(response => response.json())
        .then(data => {
            const loginNavItem = document.getElementById('loginNavItem');
            const loginLink = document.getElementById('loginLink');
            const navbarNav = loginNavItem.parentElement;

            if (data.authenticated) {
                // Cập nhật giao diện khi đã đăng nhập
                loginLink.innerHTML = `<i class="bi bi-person"></i> ${data.user.name}`;
                loginLink.href = "#";
                loginLink.removeAttribute('data-bs-target');
                loginLink.removeAttribute('data-bs-toggle');

                // Xóa nút đăng xuất cũ (nếu có) để tránh trùng lặp
                const existingLogout = navbarNav.querySelector('.nav-item.logout');
                if (existingLogout) {
                    existingLogout.remove();
                }

                // Thêm nút đăng xuất
                const logoutItem = document.createElement('li');
                logoutItem.className = 'nav-item logout';
                logoutItem.innerHTML = '<a class="nav-link" href="/logout">Đăng Xuất</a>';
                navbarNav.appendChild(logoutItem);
            } else {
                // Hiển thị nút đăng nhập nếu chưa đăng nhập
                loginLink.innerHTML = 'Đăng Nhập';
                loginLink.setAttribute('data-bs-toggle', 'modal');
                loginLink.setAttribute('data-bs-target', '#loginModal');
            }
        })
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
    document.querySelector('a[href="#product-section"]').addEventListener("click", function (event) {
        event.preventDefault(); // Ngăn chặn hành vi nhảy trang mặc định
        document.querySelector("#product-section").scrollIntoView({behavior: "smooth"});
    });
});


/*pop up cart*/
const openBtn = document.getElementById('openCartSidebar');
const closeBtn = document.getElementById('closeCartSidebar');
const cartSidebar = document.getElementById('cartSidebar');

openBtn.addEventListener('click', () => {
    cartSidebar.classList.add('active');
});

closeBtn.addEventListener('click', () => {
    cartSidebar.classList.remove('active');
});

// Đóng khi nhấn ngoài
window.addEventListener('click', (e) => {
    if (!cartSidebar.contains(e.target) && !openBtn.contains(e.target)) {
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

    // Cập nhật tổng tiền tương ứng
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

// Khởi tạo
const sidebarCart = document.getElementById("cartSidebar");
const modalCart = document.getElementById("cartModal");

attachQuantityHandlers(sidebarCart);
attachQuantityHandlers(modalCart);
updateTotal(sidebarCart);
updateTotal(modalCart);


// phương thức thanh toán
const btnCOD = document.getElementById("btnCOD");
const btnOnline = document.getElementById("btnOnline");

btnCOD.addEventListener("click", () => {
    btnCOD.classList.add("active");
    btnOnline.classList.remove("active");
});

btnOnline.addEventListener("click", () => {
    btnOnline.classList.add("active");
    btnCOD.classList.remove("active");
});


// api hành chính VN chọn quận huyện
const provinceSelect = document.getElementById("provinceSelect");
const districtSelect = document.getElementById("districtSelect");

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

// Khi chọn tỉnh, gọi API để lấy quận/huyện tương ứng
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
document.addEventListener("DOMContentLoaded", function () {
    // Kiểm tra trạng thái đăng nhập
    fetch('/check-auth', {
        credentials: 'include' // Gửi cookie
    })
        .then(response => response.json())
        .then(data => {
            const loginLink = document.querySelector('a[data-bs-target="#loginModal"]');
            if (data.authenticated) {
                // Cập nhật giao diện khi đã đăng nhập
                loginLink.innerHTML = `<i class="bi bi-person"></i> ${data.user.name}`;
                loginLink.href = "#";
                loginLink.removeAttribute('data-bs-target');
                loginLink.removeAttribute('data-bs-toggle');
                // Thêm nút đăng xuất
                const logoutItem = document.createElement('li');
                logoutItem.className = 'nav-item';
                logoutItem.innerHTML = '<a class="nav-link" href="/logout">Đăng Xuất</a>';
                document.querySelector('.navbar-nav').appendChild(logoutItem);
            }
        })
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
// js/utils.js
const API_BASE_URL = '';

// Gọi API với fetch
async function callApi(endpoint, method = 'GET', data = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json'
        }
    };
    if (data) {
        options.body = JSON.stringify(data);
    }
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error(`Lỗi khi gọi API ${endpoint}:`, error);
        Swal.fire({
            icon: 'error',
            title: 'Lỗi',
            text: `Không thể tải dữ liệu: ${error.message}`,
        });
        throw error;
    }
}

// Hiển thị thông báo SweetAlert
function showAlert(title, text, icon = 'success') {
    return Swal.fire({
        title,
        text,
        icon,
        confirmButtonText: 'OK'
    });
}

// Tạo phân trang
function renderPagination(paginationUl, currentPage, totalPages, onPageChange) {
    paginationUl.innerHTML = '';
    for (let i = 0; i < totalPages; i++) {
        const li = document.createElement('li');
        li.className = `page-item ${i === currentPage ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="#">${i + 1}</a>`;
        li.addEventListener('click', (e) => {
            e.preventDefault();
            onPageChange(i);
        });
        paginationUl.appendChild(li);
    }
}
// Lấy các phần tử DOM
const searchInput = document.getElementById('searchInput');
const searchButton = document.getElementById('searchButton');
const searchSuggestions = document.getElementById('searchSuggestions');

// Hàm debounce để hạn chế số lần gọi API khi người dùng nhập nhanh
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Hàm tìm kiếm và hiển thị gợi ý
const fetchSuggestions = debounce(async (query) => {
    if (query.length < 1) { // Tìm kiếm ngay khi gõ 1 ký tự
        searchSuggestions.style.display = 'none';
        searchSuggestions.innerHTML = '';
        return;
    }

    try {
        const response = await fetch(`/api/products/search?query=${encodeURIComponent(query)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
            }
        });

        if (!response.ok) {
            throw new Error('Lỗi mạng hoặc server');
        }

        const data = await response.json();
        searchSuggestions.innerHTML = ''; // Xóa gợi ý cũ

        if (data.length > 0) {
            data.forEach(product => {
                const suggestionItem = document.createElement('a');
                suggestionItem.href = `/detail?id=${product.id}`;
                suggestionItem.classList.add('list-group-item', 'list-group-item-action', 'd-flex', 'align-items-center');
                suggestionItem.innerHTML = `
                    <img src="${product.imageUrl || '/images/default-image.jpg'}" alt="${product.name}" style="width: 50px; height: 50px; object-fit: cover; margin-right: 10px;">
                    <div>
                        <strong>${product.name}</strong><br>
                        <small>${product.price.toLocaleString('vi-VN')} đ</small>
                    </div>
                `;
                searchSuggestions.appendChild(suggestionItem);
            });
            searchSuggestions.style.display = 'block';
        } else {
            searchSuggestions.innerHTML = '<div class="list-group-item">Không tìm thấy sản phẩm</div>';
            searchSuggestions.style.display = 'block';
        }
    } catch (error) {
        console.error('Lỗi khi tìm kiếm:', error);
        searchSuggestions.innerHTML = '<div class="list-group-item">Có lỗi xảy ra</div>';
        searchSuggestions.style.display = 'block';
    }
}, 300);

// Lắng nghe sự kiện nhập vào ô tìm kiếm
searchInput.addEventListener('input', (e) => {
    const query = e.target.value.trim();
    fetchSuggestions(query);
});

// Ẩn gợi ý khi nhấp ra ngoài
document.addEventListener('click', (e) => {
    if (!searchInput.contains(e.target) && !searchSuggestions.contains(e.target)) {
        searchSuggestions.style.display = 'none';
    }
});

// Hiển thị lại gợi ý khi ô tìm kiếm được focus
searchInput.addEventListener('focus', () => {
    const query = searchInput.value.trim();
    if (query.length >= 1) {
        fetchSuggestions(query);
    }
});

// Xử lý khi nhấn nút tìm kiếm
searchButton.addEventListener('click', (e) => {
    e.preventDefault();
    const query = searchInput.value.trim();
    if (query.length > 0) {
        window.location.href = `/search?query=${encodeURIComponent(query)}`;
    } else {
        Swal.fire({
            icon: 'warning',
            title: 'Vui lòng nhập từ khóa',
            text: 'Nhập ít nhất 1 ký tự để tìm kiếm.',
        });
    }
});

// Xử lý khi nhấn Enter trong ô tìm kiếm
searchInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        e.preventDefault();
        searchButton.click();
    }
});
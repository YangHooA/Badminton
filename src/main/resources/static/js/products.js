// js/products.js
document.addEventListener('DOMContentLoaded', () => {
    loadProducts();

    // Xử lý tìm kiếm sản phẩm
    document.getElementById('search-products').addEventListener('input', (e) => {
        loadProducts(0, e.target.value);
    });

    // Xử lý lọc theo danh mục
    document.getElementById('filter-category').addEventListener('change', (e) => {
        loadProducts(0, document.getElementById('search-products').value, e.target.value);
    });
});

async function loadProducts(page = 0, keyword = '', category = '') {
    try {
        const response = await callApi(`/api/products?page=${page}&size=10${keyword ? `&keyword=${keyword}` : ''}${category ? `&category=${category}` : ''}`);
        const tbody = document.getElementById('products-tbody');
        tbody.innerHTML = '';

        response.content.forEach(product => {
            const row = `
                <tr>
                    <td>${product.id}</td>
                    <td>
                        <img src="${product.imageUrl}" width="50" height="50" style="margin-right: 8px;" />
                        ${product.name}
                    </td>
                    <td>${product.price.toLocaleString('vi-VN')} VNĐ</td>
                    <td>${product.typeName || ''}</td>
                    <td>${product.playStyleName || ''}</td>
                    <td>${product.skillLevelName || ''}</td>
                    <td>${product.weightName || ''}</td>
                    <td>
                        <button class="btn btn-info btn-sm" onclick="showProductDetail(${product.id})">Chi tiết</button>
                        <button class="btn btn-warning btn-sm" onclick="editProduct(${product.id})">Sửa</button>
                        <button class="btn btn-danger btn-sm" onclick="deleteProduct(${product.id})">Xóa</button>
                    </td>
                </tr>`;
            tbody.insertAdjacentHTML('beforeend', row);
        });

        // Render phân trang
        renderPagination(
            document.getElementById('products-pagination'),
            response.number,
            response.totalPages,
            (newPage) => loadProducts(newPage, keyword, category)
        );
    } catch (error) {
        console.error('Lỗi khi tải sản phẩm:', error);
    }
}

async function showProductDetail(id) {
    try {
        const product = await callApi(`/api/products/${id}`);
        const modal = new bootstrap.Modal(document.getElementById('productDetailModal'));

        // Cập nhật thông tin sản phẩm
        document.getElementById('detail_ten_san_pham').value = product.name;
        document.getElementById('detail_thuong_hieu').value = product.typeName;
        document.getElementById('detail_mo_ta_ngan').value = product.description;

        // Cập nhật danh sách ảnh
        const imageTbody = document.getElementById('detail_image_table_body');
        imageTbody.innerHTML = '';
        product.images?.forEach(image => {
            const row = `
                <tr>
                    <td>${image.url}</td>
                    <td>${image.isMain ? 'Có' : 'Không'}</td>
                    <td>${image.order}</td>
                    <td><img src="${image.url}" width="50" height="50" /></td>
                    <td>
                        <button class="btn btn-danger btn-sm" onclick="deleteImage(${image.id})">Xóa</button>
                    </td>
                </tr>`;
            imageTbody.insertAdjacentHTML('beforeend', row);
        });

        modal.show();
    } catch (error) {
        console.error('Lỗi khi tải chi tiết sản phẩm:', error);
    }
}

async function editProduct(id) {
    // Chuyển sang chế độ chỉnh sửa trong modal
    showProductDetail(id);
    document.getElementById('editProductBtn').style.display = 'none';
    document.getElementById('saveProduc').style.display = 'inline-block';
    // Cho phép chỉnh sửa các trường
    document.querySelectorAll('#productDetailForm input, #productDetailForm textarea, #productDetailForm select').forEach(el => el.removeAttribute('readonly'));
}

async function deleteProduct(id) {
    const result = await Swal.fire({
        title: 'Bạn có chắc?',
        text: 'Sản phẩm này sẽ bị xóa!',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        try {
            await callApi(`/api/products/${id}`, 'DELETE');
            showAlert('Thành công', 'Sản phẩm đã được xóa!');
            loadProducts();
        } catch (error) {
            console.error('Lỗi khi xóa sản phẩm:', error);
        }
    }
}
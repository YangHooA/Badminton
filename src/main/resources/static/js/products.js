// js/products.js
document.addEventListener('DOMContentLoaded', () => {
    loadProducts();

    // Xử lý tìm kiếm sản phẩm
    document.getElementById('search-products')?.addEventListener('input', (e) => {
        loadProducts(0, e.target.value);
    });

    // Xử lý lọc theo danh mục
    document.getElementById('filter-category')?.addEventListener('change', (e) => {
        loadProducts(0, document.getElementById('search-products').value, e.target.value);
    });

    // Xử lý nút lưu sản phẩm
    document.getElementById('saveProduct')?.addEventListener('click', saveProduct);

    // Xử lý nút thêm sản phẩm mới
    document.getElementById('addProductBtn')?.addEventListener('click', openAddProductModal);
});

// Tải danh sách sản phẩm
async function loadProducts(page = 0, keyword = '', category = '') {
    try {
        // Hiển thị trạng thái đang tải
        const tbody = document.getElementById('products-tbody');
        tbody.innerHTML = '<tr><td colspan="8" class="text-center">Đang tải...</td></tr>';

        const response = await callApi(`/api/products?page=${page}&size=10${keyword ? `&keyword=${encodeURIComponent(keyword)}` : ''}${category ? `&category=${encodeURIComponent(category)}` : ''}`);

        tbody.innerHTML = '';
        if (!response.content || response.content.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center">Không có sản phẩm nào</td></tr>';
            return;
        }

        response.content.forEach(product => {
            const row = `
                <tr>
                    <td>${product.id}</td>
                    <td>
                        <img src="${product.imageUrl || 'default-image.jpg'}" width="50" height="50" style="margin-right: 8px;" />
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
        Swal.fire('Lỗi', 'Không thể tải danh sách sản phẩm: ' + error.message, 'error');
    }
}

// Hiển thị chi tiết sản phẩm
async function showProductDetail(id) {
    try {
        const product = await callApi(`/api/products/${id}`);
        const modal = new bootstrap.Modal(document.getElementById('productDetailModal'));

        // Cập nhật thông tin sản phẩm
        document.getElementById('detail_ten_san_pham').value = product.name;
        document.getElementById('detail_thuong_hieu').value = product.typeName;
        document.getElementById('detail_mo_ta_ngan').value = product.description || '';
        document.getElementById('detail_price').value = product.price;
        document.getElementById('detail_play_style').value = product.playStyleName || '';
        document.getElementById('detail_skill_level').value = product.skillLevelName || '';
        document.getElementById('detail_weight').value = product.weightName || '';

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

        // Thiết lập chế độ chỉ xem
        document.querySelectorAll('#productDetailForm input, #productDetailForm textarea, #productDetailForm select').forEach(el => {
            el.setAttribute('readonly', 'readonly');
            el.setAttribute('disabled', 'disabled');
        });
        document.getElementById('editProductBtn').style.display = 'inline-block';
        document.getElementById('saveProduct').style.display = 'none';

        modal.show();
    } catch (error) {
        console.error('Lỗi khi tải chi tiết sản phẩm:', error);
        Swal.fire('Lỗi', 'Không thể tải chi tiết sản phẩm: ' + error.message, 'error');
    }
}

// Chỉnh sửa sản phẩm
async function editProduct(id) {
    try {
        await showProductDetail(id);
        // Cho phép chỉnh sửa các trường
        document.querySelectorAll('#productDetailForm input, #productDetailForm textarea, #productDetailForm select').forEach(el => {
            el.removeAttribute('readonly');
            el.removeAttribute('disabled');
        });
        document.getElementById('editProductBtn').style.display = 'none';
        document.getElementById('saveProduct').style.display = 'inline-block';
        document.getElementById('saveProduct').dataset.productId = id; // Lưu ID sản phẩm để sử dụng khi lưu
    } catch (error) {
        console.error('Lỗi khi chuyển sang chế độ chỉnh sửa:', error);
        Swal.fire('Lỗi', 'Không thể chuyển sang chế độ chỉnh sửa: ' + error.message, 'error');
    }
}

// Lưu sản phẩm (sửa hoặc thêm mới)
async function saveProduct() {
    const productId = document.getElementById('saveProduct').dataset.productId;
    const isEditMode = !!productId;

    const productData = {
        name: document.getElementById('detail_ten_san_pham').value,
        typeName: document.getElementById('detail_thuong_hieu').value,
        description: document.getElementById('detail_mo_ta_ngan').value,
        price: parseFloat(document.getElementById('detail_price').value),
        playStyleName: document.getElementById('detail_play_style').value || null,
        skillLevelName: document.getElementById('detail_skill_level').value || null,
        weightName: document.getElementById('detail_weight').value || null
    };

    // Kiểm tra dữ liệu
    if (!productData.name || !productData.typeName || !productData.price) {
        Swal.fire('Lỗi', 'Vui lòng điền đầy đủ tên sản phẩm, thương hiệu và giá', 'error');
        return;
    }

    try {
        const method = isEditMode ? 'PUT' : 'POST';
        const url = isEditMode ? `/api/products/${productId}` : '/api/products';
        await callApi(url, method, productData);

        Swal.fire('Thành công', isEditMode ? 'Sản phẩm đã được cập nhật!' : 'Sản phẩm đã được thêm mới!', 'success');
        bootstrap.Modal.getInstance(document.getElementById('productDetailModal')).hide();
        loadProducts();
    } catch (error) {
        console.error('Lỗi khi lưu sản phẩm:', error);
        Swal.fire('Lỗi', 'Không thể lưu sản phẩm: ' + error.message, 'error');
    }
}

// Mở modal để thêm sản phẩm mới
function openAddProductModal() {
    const modal = new bootstrap.Modal(document.getElementById('productDetailModal'));

    // Xóa dữ liệu cũ
    document.getElementById('productDetailForm').reset();
    document.getElementById('detail_image_table_body').innerHTML = '';
    document.getElementById('editProductBtn').style.display = 'none';
    document.getElementById('saveProduct').style.display = 'inline-block';
    document.getElementById('saveProduct').dataset.productId = ''; // Xóa ID để thêm mới

    // Cho phép chỉnh sửa
    document.querySelectorAll('#productDetailForm input, #productDetailForm textarea, #productDetailForm select').forEach(el => {
        el.removeAttribute('readonly');
        el.removeAttribute('disabled');
    });

    modal.show();
}

// Xóa sản phẩm
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
            Swal.fire('Thành công', 'Sản phẩm đã được xóa!', 'success');
            loadProducts();
        } catch (error) {
            console.error('Lỗi khi xóa sản phẩm:', error);
            Swal.fire('Lỗi', 'Không thể xóa sản phẩm: ' + error.message, 'error');
        }
    }
}

// Xóa ảnh sản phẩm
async function deleteImage(imageId) {
    const result = await Swal.fire({
        title: 'Bạn có chắc?',
        text: 'Ảnh này sẽ bị xóa!',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        try {
            await callApi(`/api/product-images/${imageId}`, 'DELETE');
            Swal.fire('Thành công', 'Ảnh đã được xóa!', 'success');
            const productId = document.getElementById('saveProduct').dataset.productId;
            if (productId) {
                showProductDetail(productId); // Tải lại chi tiết sản phẩm
            }
        } catch (error) {
            console.error('Lỗi khi xóa ảnh:', error);
            Swal.fire('Lỗi', 'Không thể xóa ảnh: ' + error.message, 'error');
        }
    }
}
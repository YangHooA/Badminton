// js/order.js
document.addEventListener('DOMContentLoaded', () => {
    loadOrders();

    // Xử lý tìm kiếm đơn hàng
    document.getElementById('search-orders').addEventListener('input', (e) => {
        loadOrders(0, e.target.value, document.getElementById('filter-order-status').value);
    });

    // Xử lý lọc theo trạng thái
    document.getElementById('filter-order-status').addEventListener('change', (e) => {
        loadOrders(0, document.getElementById('search-orders').value, e.target.value);
    });
});

async function loadOrders(page = 0, keyword = '', status = '') {
    try {
        const response = await callApi(`/api/orders/paged?page=${page}&size=10${status ? `&status=${status}` : ''}${keyword ? `&keyword=${keyword}` : ''}`);
        const tbody = document.getElementById('orders-tbody');
        tbody.innerHTML = '';

        response.content.forEach(order => {
            const row = `
                <tr>
                    <td>${order.id}</td>
                    <td>${order.customerName}</td>
                    <td>${order.orderDate}</td>
                    <td>${order.totalPrice.toLocaleString('vi-VN')} VNĐ</td>
                    <td>${order.status}</td>
                    <td>
                        <button class="btn btn-info btn-sm" onclick="showOrderDetail(${order.id})">Chi tiết</button>
                        <button class="btn btn-danger btn-sm" onclick="deleteOrder(${order.id})">Xóa</button>
                    </td>
                </tr>`;
            tbody.insertAdjacentHTML('beforeend', row);
        });

        // Render phân trang
        renderPagination(
            document.getElementById('orders-pagination'),
            response.number,
            response.totalPages,
            (newPage) => loadOrders(newPage, keyword, status)
        );
    } catch (error) {
        console.error('Lỗi khi tải đơn hàng:', error);
    }
}

async function showOrderDetail(id) {
    try {
        const order = await callApi(`/api/orders/${id}`);
        const modal = new bootstrap.Modal(document.getElementById('orderDetailModal'));

        document.getElementById('order-detail-id').textContent = order.id;
        document.getElementById('order-detail-customer').textContent = order.customerName;
        document.getElementById('order-detail-date').textContent = order.orderDate;
        document.getElementById('order-detail-total').textContent = order.totalPrice.toLocaleString('vi-VN') + ' VNĐ';
        document.getElementById('order-detail-status').textContent = order.status;

        modal.show();
    } catch (error) {
        console.error('Lỗi khi tải chi tiết đơn hàng:', error);
    }
}

async function deleteOrder(id) {
    const result = await Swal.fire({
        title: 'Bạn có chắc?',
        text: 'Đơn hàng này sẽ bị xóa!',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        try {
            await callApi(`/api/orders/${id}`, 'DELETE');
            showAlert('Thành công', 'Đơn hàng đã được xóa!');
            loadOrders();
        } catch (error) {
            console.error('Lỗi khi xóa đơn hàng:', error);
        }
    }
}
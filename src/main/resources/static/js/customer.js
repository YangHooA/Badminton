// js/customer.js
document.addEventListener('DOMContentLoaded', () => {
    loadCustomers();

    // Xử lý tìm kiếm khách hàng
    document.getElementById('search-users').addEventListener('input', (e) => {
        loadCustomers(e.target.value);
    });
});

async function loadCustomers(keyword = '') {
    try {
        const customers = await callApi('/api/customers');
        const tbody = document.getElementById('customers-table').querySelector('tbody');
        tbody.innerHTML = '';
        const filteredCustomers = keyword
            ? customers.filter(c => c.name.toLowerCase().includes(keyword.toLowerCase()) || c.email.toLowerCase().includes(keyword.toLowerCase()))
            : customers;

        filteredCustomers.forEach(customer => {
            const row = `
                <tr>
                    <td>${customer.id}</td>
                    <td>${customer.name}</td>
                    <td>${customer.email}</td>
                    <td>${customer.phone}</td>
                    <td>${customer.address}</td>
                    <td>${customer.role}</td>
                    <td>
                        <button class="btn btn-info btn-sm" onclick="showCustomerDetail(${customer.id})">Chi tiết</button>
                    </td>
                </tr>`;
            tbody.insertAdjacentHTML('beforeend', row);
        });
    } catch (error) {
        console.error('Lỗi khi tải khách hàng:', error);
    }
}

async function showCustomerDetail(id) {
    try {
        const customer = await callApi(`/api/customers/${id}`);
        const orders = await callApi(`/api/orders/user/${id}`); // Giả định có API này
        const modal = new bootstrap.Modal(document.getElementById('userDetailModal'));

        // Cập nhật thông tin khách hàng
        document.getElementById('detail_user_name').value = customer.name;
        document.getElementById('detail_user_email').value = customer.email;
        document.getElementById('detail_user_phone').value = customer.phone;
        document.getElementById('detail_user_address').value = customer.address;
        document.getElementById('detail_user_role').value = customer.role;

        // Cập nhật lịch sử đơn hàng
        const ordersTbody = document.getElementById('user_orders_table_body');
        ordersTbody.innerHTML = '';
        orders.forEach(order => {
            const products = order.items?.map(item => item.productName).join(', ') || '';
            const row = `
                <tr>
                    <td>${order.id}</td>
                    <td>${order.orderDate}</td>
                    <td>${order.totalPrice.toLocaleString('vi-VN')}</td>
                    <td>${order.status}</td>
                    <td>${products}</td>
                    <td>
                        <button class="btn btn-primary btn-sm" onclick="showOrderDetail(${order.id})">Xem</button>
                    </td>
                </tr>`;
            ordersTbody.insertAdjacentHTML('beforeend', row);
        });

        modal.show();
    } catch (error) {
        console.error('Lỗi khi tải chi tiết khách hàng:', error);
    }
}
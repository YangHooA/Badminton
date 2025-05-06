// js/inventory.js
document.addEventListener('DOMContentLoaded', () => {
    loadInventory();

    // Xử lý tìm kiếm kho
    document.getElementById('search-inventory').addEventListener('input', (e) => {
        loadInventory(e.target.value);
    });
});

async function loadInventory(keyword = '') {
    try {
        const inventory = await callApi('/api/inventory');
        const tbody = document.getElementById('inventory-tbody');
        tbody.innerHTML = '';

        const filteredInventory = keyword
            ? inventory.filter(i => i.productName.toLowerCase().includes(keyword.toLowerCase()))
            : inventory;

        filteredInventory.forEach(item => {
            const row = `
                <tr>
                    <td>${item.productId}</td>
                    <td>${item.productName}</td>
                    <td>${item.quantity}</td>
                    <td>${item.lastUpdated}</td>
                    <td>
                        <button class="btn btn-primary btn-sm">Sửa</button>
                    </td>
                </tr>`;
            tbody.insertAdjacentHTML('beforeend', row);
        });
    } catch (error) {
        console.error('Lỗi khi tải kho:', error);
    }
}
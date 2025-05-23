// js/reports.js
$(document).ready(function () {
    // Khởi tạo biểu đồ
    let revenueChart = null;
    let topProductsChart = null;

    // Xử lý thay đổi khoảng thời gian
    $('#report-time-range').on('change', function () {
        const range = $(this).val();
        if (range === 'custom') {
            $('#custom-date-range').show();
        } else {
            $('#custom-date-range').hide();
            loadReportData(range);
        }
    });

    // Xử lý nút áp dụng khoảng thời gian tùy chỉnh
    $('#apply-date-range').on('click', function () {
        const startDate = $('#start-date').val();
        const endDate = $('#end-date').val();
        if (startDate && endDate) {
            loadReportData('custom', startDate, endDate);
        } else {
            Swal.fire('Lỗi', 'Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc', 'error');
        }
    });

    // Hàm tải dữ liệu báo cáo
    function loadReportData(range, startDate = null, endDate = null) {
        $.ajax({
            url: '/api/reports',
            method: 'GET',
            data: {
                range: range,
                startDate: startDate,
                endDate: endDate
            },
            success: function (response) {
                if (response.success) {
                    renderRevenueChart(response.revenueData || { labels: [], values: [] });
                    renderTopProductsChart(response.topProducts || []);
                    renderOrderStatusTable(response.orderStatus || []);
                    renderTopProductsTable(response.topProducts || []);
                } else {
                    Swal.fire('Lỗi', response.message || 'Không thể tải dữ liệu báo cáo', 'error');
                    renderEmptyTables();
                }
            },
            error: function (xhr) {
                Swal.fire('Lỗi', 'Lỗi khi tải dữ liệu báo cáo: ' + xhr.statusText, 'error');
                renderEmptyTables();
            }
        });
    }

    // Hàm hiển thị bảng rỗng khi không có dữ liệu
    function renderEmptyTables() {
        const orderStatusTbody = $('#orderStatusTableBody');
        orderStatusTbody.empty().append('<tr><td colspan="3" class="text-center">Không có dữ liệu</td></tr>');

        const topProductsTbody = $('#topProductsTableBody');
        topProductsTbody.empty().append('<tr><td colspan="4" class="text-center">Không có dữ liệu</td></tr>');

        // Xóa biểu đồ nếu có
        if (revenueChart) revenueChart.destroy();
        if (topProductsChart) topProductsChart.destroy();
    }

    // Hàm hiển thị biểu đồ doanh thu
    function renderRevenueChart(data) {
        const ctx = document.getElementById('revenueChart').getContext('2d');
        if (revenueChart) revenueChart.destroy();

        revenueChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels || [],
                datasets: [{
                    label: 'Doanh thu (VNĐ)',
                    data: data.values || [],
                    borderColor: 'rgba(75, 192, 192, 1)',
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    fill: true
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function (value) {
                                return value.toLocaleString('vi-VN') + ' VNĐ';
                            }
                        }
                    }
                }
            }
        });
    }

    // Hàm hiển thị biểu đồ sản phẩm bán chạy
    function renderTopProductsChart(data) {
        const ctx = document.getElementById('topProductsChart').getContext('2d');
        if (topProductsChart) topProductsChart.destroy();

        topProductsChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.map(item => item.name || 'Không xác định'),
                datasets: [{
                    label: 'Số lượng bán',
                    data: data.map(item => item.quantity || 0),
                    backgroundColor: 'rgba(54, 162, 235, 0.6)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });
    }

    // Hàm hiển thị bảng trạng thái đơn hàng
    function renderOrderStatusTable(data) {
        const tbody = $('#orderStatusTableBody');
        tbody.empty();

        if (!data || data.length === 0) {
            tbody.append('<tr><td colspan="3" class="text-center">Không có dữ liệu</td></tr>');
            return;
        }

        data.forEach(item => {
            const row = `
                <tr>
                    <td>${item.status || 'Không xác định'}</td>
                    <td>${item.count || 0}</td>
                    <td>${(item.total || 0).toLocaleString('vi-VN')} VNĐ</td>
                </tr>`;
            tbody.append(row);
        });
    }

    // Hàm hiển thị bảng sản phẩm bán chạy
    function renderTopProductsTable(data) {
        const tbody = $('#topProductsTableBody');
        tbody.empty();

        if (!data || data.length === 0) {
            tbody.append('<tr><td colspan="4" class="text-center">Không có dữ liệu</td></tr>');
            return;
        }

        data.forEach(item => {
            const row = `
                <tr>
                    <td>${item.id || 'N/A'}</td>
                    <td>${item.name || 'Không xác định'}</td>
                    <td>${item.quantity || 0}</td>
                    <td>${(item.revenue || 0).toLocaleString('vi-VN')} VNĐ</td>
                </tr>`;
            tbody.append(row);
        });
    }

    // Xuất báo cáo PDF
    $('#exportReportPDF').on('click', function () {
        const range = $('#report-time-range').val();
        const startDate = $('#start-date').val();
        const endDate = $('#end-date').val();
        $.ajax({
            url: '/api/reports/export/pdf',
            method: 'GET',
            data: {
                range: range,
                startDate: startDate,
                endDate: endDate
            },
            xhrFields: {
                responseType: 'blob'
            },
            success: function (data) {
                const url = window.URL.createObjectURL(data);
                const a = document.createElement('a');
                a.href = url;
                a.download = `BaoCao_${range}_${new Date().toISOString()}.pdf`;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(url);
            },
            error: function () {
                Swal.fire('Lỗi', 'Không thể xuất báo cáo PDF', 'error');
            }
        });
    });

    // Xuất báo cáo Excel
    $('#exportReportExcel').on('click', function () {
        const range = $('#report-time-range').val();
        const startDate = $('#start-date').val();
        const endDate = $('#end-date').val();
        $.ajax({
            url: '/api/reports/export/excel',
            method: 'GET',
            data: {
                range: range,
                startDate: startDate,
                endDate: endDate
            },
            xhrFields: {
                responseType: 'blob'
            },
            success: function (data) {
                const url = window.URL.createObjectURL(data);
                const a = document.createElement('a');
                a.href = url;
                a.download = `BaoCao_${range}_${new Date().toISOString()}.xlsx`;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(url);
            },
            error: function () {
                Swal.fire('Lỗi', 'Không thể xuất báo cáo Excel', 'error');
            }
        });
    });

    // Tải dữ liệu báo cáo khi trang được load
    loadReportData('day');
});
// // js/reviews.js
// document.addEventListener('DOMContentLoaded', () => {
//     let currentPage = 0;
//     const pageSize = 10;
//     let currentRating = '';
//     let currentKeyword = '';
//
//     // Tải danh sách đánh giá lần đầu
//     loadReviews();
//
//     // Xử lý tìm kiếm đánh giá
//     document.getElementById('search-reviews').addEventListener('input', (e) => {
//         currentKeyword = e.target.value.trim();
//         currentPage = 0;
//         loadReviews(currentPage, currentRating, currentKeyword);
//     });
//
//     // Xử lý lọc theo số sao
//     document.getElementById('filter-review-rating').addEventListener('change', (e) => {
//         currentRating = e.target.value;
//         currentPage = 0;
//         loadReviews(currentPage, currentRating, currentKeyword);
//     });
//
//     // Xử lý nút xuất PDF
//     document.getElementById('exportReviewsPDF').addEventListener('click', exportReviewsToPDF);
// });
//
// // Hàm tải danh sách đánh giá
// async function loadReviews(page = 0, rating = '', keyword = '') {
//     const tbody = document.getElementById('reviews-tbody');
//     tbody.innerHTML = '<tr><td colspan="7" class="text-center">Đang tải...</td></tr>';
//
//     try {
//         const url = `/api/reviews?page=${page}&size=${pageSize}${rating ? `&rating=${rating}` : ''}${keyword ? `&keyword=${keyword}` : ''}`;
//         const data = await callApi(url);
//
//         tbody.innerHTML = '';
//
//         // Hiển thị danh sách đánh giá
//         if (data.content && data.content.length > 0) {
//             data.content.forEach(review => {
//                 const row = `
//                     <tr>
//                         <td>${review.id}</td>
//                         <td>${review.productName || 'N/A'}</td>
//                         <td>${review.userName || 'N/A'}</td>
//                         <td>${review.rating}</td>
//                         <td>${review.content || 'N/A'}</td>
//                         <td>${review.createdAt ? new Date(review.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</td>
//                         <td>
//                             <button class="btn btn-primary btn-sm" onclick="showReviewDetail(${review.id})">Chi tiết</button>
//                             <button class="btn btn-danger btn-sm" onclick="deleteReview(${review.id})">Xóa</button>
//                         </td>
//                     </tr>`;
//                 tbody.insertAdjacentHTML('beforeend', row);
//             });
//         } else {
//             tbody.innerHTML = '<tr><td colspan="7" class="text-center">Không có đánh giá nào</td></tr>';
//         }
//
//         // Cập nhật phân trang
//         renderPagination(
//             document.getElementById('reviews-pagination'),
//             data.number,
//             data.totalPages,
//             (newPage) => {
//                 currentPage = newPage;
//                 loadReviews(newPage, rating, keyword);
//             }
//         );
//     } catch (error) {
//         console.error('Lỗi khi tải danh sách đánh giá:', error);
//         tbody.innerHTML = '<tr><td colspan="7" class="text-center">Lỗi khi tải dữ liệu</td></tr>';
//         showAlert('Lỗi', `Không thể tải danh sách đánh giá: ${error.message}`, 'error');
//     }
// }
//
// // Hàm hiển thị chi tiết đánh giá
// async function showReviewDetail(reviewId) {
//     try {
//         const review = await callApi(`/api/reviews/${reviewId}`);
//         const modal = new bootstrap.Modal(document.getElementById('reviewDetailModal'));
//
//         // Cập nhật thông tin đánh giá
//         document.getElementById('review-detail-id').textContent = review.id;
//         document.getElementById('review-detail-product').textContent = review.productName || 'N/A';
//         document.getElementById('review-detail-user').textContent = review.userName || 'N/A';
//         document.getElementById('review-detail-rating').textContent = review.rating;
//         document.getElementById('review-detail-content').textContent = review.content || 'N/A';
//         document.getElementById('review-detail-date').textContent = review.createdAt ? new Date(review.createdAt).toLocaleDateString('vi-VN') : 'N/A';
//
//         // Gắn sự kiện xóa vào nút trong modal
//         document.getElementById('deleteReviewBtn').onclick = () => deleteReview(reviewId);
//
//         modal.show();
//     } catch (error) {
//         console.error('Lỗi khi tải chi tiết đánh giá:', error);
//         showAlert('Lỗi', `Không thể tải chi tiết đánh giá: ${error.message}`, 'error');
//     }
// }
//
// // Hàm xóa đánh giá
// async function deleteReview(reviewId) {
//     const result = await Swal.fire({
//         title: 'Xác nhận xóa',
//         text: `Bạn có chắc muốn xóa đánh giá với ID: ${reviewId}?`,
//         icon: 'warning',
//         showCancelButton: true,
//         confirmButtonText: 'Xóa',
//         cancelButtonText: 'Hủy'
//     });
//
//     if (result.isConfirmed) {
//         try {
//             await callApi(`/api/reviews/${reviewId}`, 'DELETE');
//             showAlert('Thành công', 'Đánh giá đã được xóa!');
//             const modal = bootstrap.Modal.getInstance(document.getElementById('reviewDetailModal'));
//             modal.hide();
//             loadReviews();
//         } catch (error) {
//             console.error('Lỗi khi xóa đánh giá:', error);
//             showAlert('Lỗi', `Không thể xóa đánh giá: ${error.message}`, 'error');
//         }
//     }
// }
//
// // Hàm xuất danh sách đánh giá ra PDF
// function exportReviewsToPDF() {
//     const { jsPDF } = window.jspdf;
//     const doc = new jsPDF();
//     doc.autoTable({
//         head: [['ID', 'Sản phẩm', 'Người dùng', 'Số sao', 'Nội dung', 'Ngày đánh giá']],
//         body: Array.from(document.querySelectorAll('#reviews-tbody tr')).map(row => [
//             row.cells[0].textContent,
//             row.cells[1].textContent,
//             row.cells[2].textContent,
//             row.cells[3].textContent,
//             row.cells[4].textContent,
//             row.cells[5].textContent
//         ])
//     });
//     doc.save('reviews.pdf');
// }
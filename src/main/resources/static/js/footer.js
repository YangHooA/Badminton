document.addEventListener("DOMContentLoaded", function () {
    document.querySelector('a[href="#footer"]').addEventListener("click", function (event) {
        event.preventDefault(); // Ngăn chặn hành vi nhảy trang mặc định
        document.querySelector("#footer").scrollIntoView({behavior: "smooth"});
    });
});

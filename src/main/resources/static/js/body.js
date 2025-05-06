const minPrice = document.getElementById("minPrice");
const maxPrice = document.getElementById("maxPrice");
const minValue = document.getElementById("minValue");
const maxValue = document.getElementById("maxValue");
const sliderTrack = document.querySelector(".slider-track");

function updateSlider() {
    let min = parseInt(minPrice.value);
    let max = parseInt(maxPrice.value);
    let minLimit = 1000000; // Khoảng cách tối thiểu giữa min và max

    // Đảm bảo khoảng cách tối thiểu 1.000.000 VND
    if (max - min < minLimit) {
        if (this.id === "minPrice") {
            min = max - minLimit;
            minPrice.value = min;
        } else {
            max = min + minLimit;
            maxPrice.value = max;
        }
    }

    // Cập nhật hiển thị giá trị
    minValue.innerText = min.toLocaleString();
    maxValue.innerText = max.toLocaleString();

    // Tính phần trăm vị trí của min và max
    let percentMin = (min / 20000000) * 100;
    let percentMax = (max / 20000000) * 100;

    // Cập nhật thanh màu xanh nằm giữa hai nút tròn
    sliderTrack.style.left = percentMin + "%";
    sliderTrack.style.width = (percentMax - percentMin) + "%";
}

// Lắng nghe sự kiện kéo thanh slider
minPrice.addEventListener("input", updateSlider);
maxPrice.addEventListener("input", updateSlider);

// Gọi update lần đầu để hiển thị đúng trạng thái ban đầu
updateSlider();

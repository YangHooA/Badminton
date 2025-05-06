-- 1. Tạo bảng types_of_product
CREATE TABLE types_of_product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    describle TEXT
);


--2. Tạo bảng sản phẩm
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(15,2) NOT NULL,
    type_id INT NOT NULL,  -- Khóa ngoại liên kết với bảng types_of_product
    play_style_id INT,  -- Khóa ngoại liên kết với bảng play_style
    skill_level_id INT,  -- Khóa ngoại liên kết với bảng skill_level
    weight_id INT,  -- Khóa ngoại liên kết với bảng weight
    FOREIGN KEY (type_id) REFERENCES types_of_product(id) ON DELETE CASCADE,
    FOREIGN KEY (play_style_id) REFERENCES play_style(id) ON DELETE SET NULL,
    FOREIGN KEY (skill_level_id) REFERENCES skill_level(id) ON DELETE SET NULL,
    FOREIGN KEY (weight_id) REFERENCES weight(id) ON DELETE SET NULL
);
-- Tránh trùng tên sản phẩm
ALTER TABLE products ADD CONSTRAINT unique_product_name UNIQUE (name);

--3. Tạo bảng ảnh sp
CREATE TABLE product_images (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL,  -- Khóa ngoại liên kết với bảng products
    image_url VARCHAR(255),   -- Đường dẫn tới hình ảnh (hoặc có thể là BYTEA nếu lưu hình ảnh dưới dạng nhị phân)
    image_data BYTEA,         -- Dữ liệu nhị phân của hình ảnh (tuỳ chọn nếu bạn muốn lưu hình ảnh trực tiếp)
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);


--4. Tạo bảng play_style
CREATE TABLE play_style (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    note TEXT
);

--5. Tạo bảng skill_level
CREATE TABLE skill_level (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    note TEXT
);

--6. Tạo bảng weight
CREATE TABLE weight (
    id SERIAL PRIMARY KEY,
    name VARCHAR(10) NOT NULL,
    note TEXT
);


--7. Tạo bảng tồn kho
CREATE TABLE inventory (
    product_id INT PRIMARY KEY,  -- Làm khóa chính, không cần id riêng
    quantity INT NOT NULL DEFAULT 0,  -- Số lượng sản phẩm trong kho
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- Ngày cập nhật cuối cùng
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);


--8. Tạo bảng khách hàng
CREATE TABLE customers (
    id SERIAL PRIMARY KEY,  -- Mã khách hàng tự động tăng
    name VARCHAR(255) NOT NULL,  -- Tên khách hàng
    email VARCHAR(255) UNIQUE NOT NULL,  -- Email (dùng để đăng nhập)
    phone VARCHAR(20)  NULL,  -- Số điện thoại
    address TEXT,  -- Địa chỉ mặc định
    password_hash TEXT NOT NULL,  -- Mật khẩu đã mã hóa
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- Ngày tạo tài khoản
	
);
-- Tránh trùng email khách hàng
ALTER TABLE customers ADD CONSTRAINT unique_customer_email UNIQUE (email);
--thêm role
ALTER TABLE customers ADD COLUMN role VARCHAR(20) CHECK (role IN ('ADMIN', 'USER')) DEFAULT 'USER';



--9. Tạo bảng hóa đơn
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_id INT NULL,  -- Nếu là khách vãng lai, giá trị này sẽ NULL
    guest_name VARCHAR(255),  -- Lưu thông tin khách vãng lai
    guest_email VARCHAR(255),
    guest_phone VARCHAR(20),
    guest_address TEXT,
    total_price NUMERIC(15,2) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('Pending', 'Completed', 'Cancelled')) DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL
);


--10. Tạo bảng chi tiết hóa đơn
CREATE TABLE order_details (
    id SERIAL PRIMARY KEY,  -- Mã chi tiết đơn hàng tự động tăng
    order_id INT NOT NULL,  -- Khóa ngoại liên kết với bảng orders
    product_id INT NOT NULL,  -- Khóa ngoại liên kết với bảng products
    quantity INT NOT NULL CHECK (quantity > 0),  -- Số lượng sản phẩm
    price NUMERIC(15,2) NOT NULL CHECK (price >= 0),  -- Giá sản phẩm tại thời điểm mua
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,  -- Xóa chi tiết khi xóa đơn hàng
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE   -- Xóa chi tiết khi xóa sản phẩm
);

-- Index giúp tăng tốc độ lọc theo ngày tạo hóa đơn
CREATE INDEX idx_orders_created_at ON orders (created_at);

--11. Tạo bảng giỏ hàng
CREATE TABLE cart (
    id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


--12. Tạo bảng cart_items
CREATE TABLE cart_items (
    id SERIAL PRIMARY KEY,
    cart_id INTEGER REFERENCES cart(id),
    product_id INTEGER REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Thêm ràng buộc UNIQUE
ALTER TABLE cart_items
ADD CONSTRAINT unique_cart_product UNIQUE (cart_id, product_id);
-- Nếu sản phẩm đã có trong giỏ thì cập nhật số lượng
INSERT INTO cart_items (cart_id, product_id, quantity)
VALUES (1, 13, 2)  -- ví dụ cart_id = 1, sản phẩm id = 3, số lượng = 2
ON CONFLICT (cart_id, product_id)
DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity;



SELECT p.name, p.price, ci.quantity, (p.price * ci.quantity) AS total_price
FROM cart_items ci
JOIN products p ON ci.product_id = p.id
WHERE ci.cart_id = 1;




--1. Chèn dl bảng loại sp
INSERT INTO types_of_product (name, describle) VALUES
('YONEX', 'Thương hiệu cầu lông nổi tiếng của Nhật Bản'),
('LINNING', 'Hãng thể thao của Trung Quốc'),
('VICTOR', 'Hãng vợt cầu lông đến từ Đài Loan'),
('MIZUNO', 'Thương hiệu thể thao của Nhật Bản'),
('KIMPOO', 'Một hãng sản xuất vợt ít phổ biến'),
('PROACE', 'Hãng vợt phổ biến với người chơi phong trào'),
('VS', 'Một thương hiệu mới nổi'),
('FELET', 'Hãng sản xuất vợt cầu lông chuyên nghiệp');


--2. Chèn dl bảng sp
INSERT INTO products (name, price, type_id, play_style_id, skill_level_id, weight_id) VALUES
('YONEX Astrox 99', 4000000.00, 1, 1, 4, 3),
('LINNING N90 III', 3500000.00, 2, 2, 2, 4),
('VICTOR Brave Sword 12', 4200000.00, 3, 3, 4, 2),
('MIZUNO TX-700', 4500000.00, 4, 1, 3, 5),
('KIMPOO Gold 500', 3000000.00, 5, 2, 1, 6),
('PROACE T1000', 2800000.00, 6, 3, 2, 4),
('VS Master Series', 5000000.00, 7, 1, 4, 2),
('FELET Power 88', 3700000.00, 8, 2, 3, 5),
('YONEX Voltric Z-Force II', 4800000.00, 1, 3, 4, 2),
('LINNING Windstorm', 3200000.00, 2, 1, 3, 7);


--3. Chèn hình ảnh cho sản phẩm
INSERT INTO product_images (product_id, image_url) VALUES
(1, 'https://example.com/images/yonex_astrox_99_1.jpg'),
(1, 'https://example.com/images/yonex_astrox_99_2.jpg'),
(2, 'https://example.com/images/linning_n90_iii_1.jpg'),
(3, 'https://example.com/images/victor_brave_sword_12_1.jpg'),
(4, 'https://example.com/images/mizuno_tx_700_1.jpg'),
(5, 'https://example.com/images/kimpoo_gold_500_1.jpg'),
(6, 'https://example.com/images/proace_t1000_1.jpg'),
(7, 'https://example.com/images/vs_master_series_1.jpg'),
(8, 'https://example.com/images/felet_power_88_1.jpg'),
(9, 'https://example.com/images/yonex_voltric_z_force_ii_1.jpg');


--4. Chèn dữ liệu vào bảng play_style
INSERT INTO play_style (name, note) VALUES
('Công thủ toàn diện', 'Phù hợp cho cả hai phong cách chơi công và thủ'),
('Phản tạt phòng thủ', 'Chuyên dùng cho lối chơi phòng thủ'),
('Tấn công', 'Chuyên dùng cho lối chơi tấn công');


--5. Chèn dữ liệu vào bảng skill_level
INSERT INTO skill_level (name, note) VALUES
('Mới chơi', 'Dành cho người mới bắt đầu'),
('Trung bình', 'Dành cho người chơi có kinh nghiệm vừa phải'),
('Trung bình khá', 'Dành cho người chơi có kỹ năng tương đối tốt'),
('Khá tốt', 'Dành cho người chơi có kỹ năng tốt');


--6. Chèn dữ liệu vào bảng weight
INSERT INTO weight (name, note) VALUES
('2F', 'Trọng lượng nhẹ, dễ điều khiển'),
('2U', 'Trọng lượng trung bình, cân bằng giữa công và thủ'),
('3U', 'Trọng lượng nặng hơn, thích hợp cho người chơi chuyên nghiệp'),
('4U', 'Trọng lượng nhẹ, phù hợp cho người chơi yêu thích tốc độ'),
('5U', 'Trọng lượng nhẹ, dễ điều khiển, phù hợp cho người mới chơi'),
('6U', 'Trọng lượng rất nhẹ, dễ kiểm soát, dành cho người chơi chuyên nghiệp'),
('7U', 'Trọng lượng cực nhẹ, thích hợp cho lối chơi tốc độ');


--7. Chèn dữ liệu vào bảng inventory
INSERT INTO inventory (product_id, quantity)
VALUES
(11, 100),
(12, 90),
(13, 80),
(14, 70),
(15, 60),
(16, 50),
(17, 40),
(18, 30),
(19, 20),
(20, 10);

--8. Chèn dl bảng khách
INSERT INTO customers (name, email, phone, address, password_hash, role) VALUES
('Trần Hồng Ánh', 'at942388@gmail.com', '0399108582', '178, Cổ Nhuế, Hà Nội', '123456', 'ADMIN'),
('Nguyễn Minh Tú', 'tuminh98@gmail.com', '0968123456', '45, Trần Duy Hưng, Hà Nội', '654321', 'USER'),
('Lê Quốc Bảo', 'baole123@gmail.com', '0909789654', '23, Nguyễn Văn Cừ, TP. Hồ Chí Minh', 'abcdef', 'USER'),
('Phạm Thu Thảo', 'thaothu11@gmail.com', '0933456789', '99, Lê Duẩn, Đà Nẵng', 'qwerty', 'USER'),
('Đặng Văn Long', 'longdv2000@gmail.com', '0912233445', '12, Nguyễn Trãi, Hải Phòng', 'zxcvb', 'USER');

--9. Chèn dl bảng hóa đơn
INSERT INTO orders (customer_id, guest_name, guest_email, guest_phone, guest_address, total_price, status) 
VALUES 
(1, NULL, NULL, NULL, NULL, 2000000.00, 'Pending'),  -- Đơn hàng của khách hàng có ID 1
(2, NULL, NULL, NULL, NULL, 3000000.00, 'Completed'), -- Đơn hàng của khách hàng có ID 2
(3, NULL, NULL, NULL, NULL, 2500000.00, 'Cancelled'), -- Đơn hàng của khách hàng có ID 3
(NULL, 'Nguyễn Văn A', 'nguyenvana@example.com', '0912345678', 'Hà Nội, Việt Nam', 1500000.00, 'Pending'),  -- Khách vãng lai
(NULL, 'Trần Thị B', 'tranthib@example.com', '0987654321', 'TP.HCM, Việt Nam', 1800000.00, 'Completed');  -- Khách vãng lai

--10. Chèn dữ liệu vào bảng order_details với ID sản phẩm cụ thể
INSERT INTO order_details (order_id, product_id, quantity, price)
VALUES
(2, 11, 2, (SELECT price FROM products WHERE id = 11)),  
(2, 12, 1, (SELECT price FROM products WHERE id = 12));




--truy vấn join bảng products
SELECT 
    p.id AS product_id,
    p.name AS product_name,
    p.price,
    tp.name AS type_name,
    ps.name AS play_style,
    sl.name AS skill_level,
    w.name AS weight
FROM 
    products p
JOIN 
    types_of_product tp ON p.type_id = tp.id
JOIN 
    play_style ps ON p.play_style_id = ps.id
JOIN 
    skill_level sl ON p.skill_level_id = sl.id
JOIN 
    weight w ON p.weight_id = w.id;

--truy vấn join bảng orders
SELECT 
    o.id AS order_id,
    COALESCE(c.name, o.guest_name) AS customer_name,
    COALESCE(c.email, o.guest_email) AS customer_email,
    o.guest_phone,
    o.guest_address,
    o.total_price,
    o.status,
    o.created_at,
    
    od.product_id,
    p.name AS product_name,
    od.quantity,
    od.price AS price_each,
    (od.quantity * od.price) AS total_item_price

FROM orders o
LEFT JOIN customers c ON o.customer_id = c.id
JOIN order_details od ON o.id = od.order_id
JOIN products p ON od.product_id = p.id

ORDER BY o.created_at DESC;

--truy vấn hóa đơn chi tiết
SELECT 
    od.id AS order_detail_id,
    o.id AS order_id,
    COALESCE(c.name, o.guest_name) AS customer_name,  -- Nếu khách đã đăng ký thì lấy tên từ customers, nếu không lấy từ guest_name
    c.phone AS customer_phone,  -- Số điện thoại của khách hàng
    c.address AS customer_address,  -- Địa chỉ của khách hàng
    p.name AS product_name,  -- Tên sản phẩm
    od.quantity,  -- Số lượng sản phẩm
    od.price,  -- Giá sản phẩm
    (od.quantity * od.price) AS total_price  -- Tổng giá cho sản phẩm trong đơn hàng
FROM order_details od
JOIN orders o ON od.order_id = o.id
LEFT JOIN customers c ON o.customer_id = c.id  -- Liên kết với bảng customers để lấy thông tin khách
JOIN products p ON od.product_id = p.id
WHERE o.id = 1;  -- Thay đổi điều kiện WHERE nếu bạn cần xem đơn hàng khác


--truy vấn bảng order_details JOIN vào tất cả các bảng cần thiết
SELECT 
    od.id AS order_detail_id,
    o.id AS order_id,
    COALESCE(c.name, o.guest_name) AS customer_name, 
    COALESCE(c.email, o.guest_email) AS customer_email,
    COALESCE(c.phone, o.guest_phone) AS customer_phone,
    p.id AS product_id,
    p.name AS product_name,
    t.name AS product_type,
    ps.name AS play_style,
    sl.name AS skill_level,
    w.name AS weight,
    od.quantity,
    od.price,
    (od.quantity * od.price) AS total_price,
    o.status AS order_status,
    o.created_at AS order_date
FROM order_details od
JOIN orders o ON od.order_id = o.id
LEFT JOIN customers c ON o.customer_id = c.id  -- Join để lấy thông tin khách hàng (nếu có)
JOIN products p ON od.product_id = p.id
JOIN types_of_product t ON p.type_id = t.id
LEFT JOIN play_style ps ON p.play_style_id = ps.id
LEFT JOIN skill_level sl ON p.skill_level_id = sl.id
LEFT JOIN weight w ON p.weight_id = w.id
ORDER BY o.created_at DESC;


--Truy vấn bảng kho
SELECT 
    inv.product_id,
    p.name AS product_name,
    p.price,
    tp.name AS type_name,
    ps.name AS play_style,
    sl.name AS skill_level,
    w.name AS weight,
    inv.quantity,
    inv.last_updated
FROM 
    inventory inv
JOIN 
    products p ON inv.product_id = p.id
JOIN 
    types_of_product tp ON p.type_id = tp.id
LEFT JOIN 
    play_style ps ON p.play_style_id = ps.id
LEFT JOIN 
    skill_level sl ON p.skill_level_id = sl.id
LEFT JOIN 
    weight w ON p.weight_id = w.id;


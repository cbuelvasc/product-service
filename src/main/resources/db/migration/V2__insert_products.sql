-- Initial data for comparison API demonstration (specifications as JSON).
INSERT INTO products (name, description, price, size, weight, color, image_url, rating, product_type, specifications)
VALUES
    ('Smartphone Alpha X1', 'Smartphone with AMOLED display and 108MP camera.', 449.99, '6.2"', '180g', 'Black',
     'https://example.com/img/alpha-x1.png', 4.5, 'SMARTPHONE',
     JSON_OBJECT(
         KEY 'batteryCapacityMah' VALUE 5000,
         KEY 'cameraSpecs' VALUE '108MP main, 12MP ultra wide, 8MP tele',
         KEY 'memoryGb' VALUE 8,
         KEY 'storageGb' VALUE 128,
         KEY 'brand' VALUE 'Alpha',
         KEY 'modelVersion' VALUE 'X1',
         KEY 'operatingSystem' VALUE 'Android 14'
     )),
    ('Smartphone Beta Pro', 'Premium performance and long-lasting battery.', 599.99, '6.5"', '195g', 'Blue',
     'https://example.com/img/beta-pro.png', 4.7, 'SMARTPHONE',
     JSON_OBJECT(
         KEY 'batteryCapacityMah' VALUE 5500,
         KEY 'cameraSpecs' VALUE '50MP main, 12MP ultra wide',
         KEY 'memoryGb' VALUE 12,
         KEY 'storageGb' VALUE 256,
         KEY 'brand' VALUE 'Beta',
         KEY 'modelVersion' VALUE 'Pro',
         KEY 'operatingSystem' VALUE 'Android 14'
     )),
    ('Run Max Sneakers', 'Lightweight running shoes with reactive cushioning.', 129.99, '42', '280g', 'White/Red',
     'https://example.com/img/run-max.png', 4.3, 'GENERIC', NULL),
    ('Urban Backpack 20L', 'Water-resistant backpack with laptop compartment.', 59.99, '20L', '650g', 'Grey',
     'https://example.com/img/backpack-20.png', 4.6, 'GENERIC', NULL);

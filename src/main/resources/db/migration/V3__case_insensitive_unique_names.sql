CREATE UNIQUE INDEX uk_categories_name_lower ON categories (LOWER(name));
CREATE UNIQUE INDEX uk_products_sku_lower ON products (LOWER(sku));
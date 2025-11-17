
Cách chạy unit tests (Windows - cmd)
- Chạy toàn bộ unit tests:
.\mvnw.cmd test

- Chạy 1 class test cụ thể (vd: `CartControllerTest`):
.\mvnw.cmd -Dtest=CartControllerTest test

- Chạy 1 method cụ thể trong class (vd: `addProductToCart_createsCartInSession`):
.\mvnw.cmd -Dtest=CartControllerTest#addProductToCart_createsCartInSession test



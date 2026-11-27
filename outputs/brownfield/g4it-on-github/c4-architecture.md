---
title: "C4 Architecture Model"
status: validated
source: "c4-architecture station"
---

## C4 Model Representation

### Level 1: System Context
- User [Person] -> G4IT System [System]
- G4IT System -> External APIs [External System]

### Level 2: Container
- Web App [Container: React]
- API Server [Container: Node.js/Python]
- Database [Container: PostgreSQL]
- Cache [Container: Redis]

### Level 3: Component
- Auth Controller
- Data Service
- Report Engine
- API Gateway

### Level 4: Code
- Identified from code graph analysis
- 1,447 files, 1,115 symbols indexed

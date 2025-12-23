import { generateService } from '@umijs/openapi'

generateService({
  requestLibPath: "import request from '@/api'",
  schemaPath: 'http://localhost:8123/api/v2/api-docs', // 后端 Swagger 文档地址
  serversPath: './src',
})

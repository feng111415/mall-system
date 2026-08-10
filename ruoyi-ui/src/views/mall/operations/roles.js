const full = { type: 'full', label: '可操作' }
const read = { type: 'read', label: '只读' }
const none = { type: 'none', label: '—' }

export const roleDefinitions = [
  {
    key: 'mall_ops_lead',
    code: 'ROLE 01',
    name: '运营主管',
    objective: '协调商城全链路，处理跨岗位异常并监督关键写操作。',
    writes: '商品、库存、履约、售后、资金',
    forbidden: '不配置用户、角色、菜单等若依系统级权限',
    responsibilities: ['监督商品、订单、履约、售后与资金链路', '处理跨岗位异常并确认责任归属', '复核库存、物流、退款和对账关键操作'],
    latestChange: '新增经营分析中心，可查看销售、商品、会员、履约、售后与资金的全部聚合数据。',
    features: [
      { name: '经营分析中心', access: '只读', description: '查看全域经营聚合、趋势、商品排行与风险指标，不展示会员明细。', path: '/mall/overview/analytics' },
      { name: '商品库存中心', access: '可操作', description: '查看商品与 SKU 库存风险，并进入商品、库存模块处理。', path: '/mall/product-inventory/product-inventory-center' },
      { name: '订单运营中心', access: '只读', description: '查询订单全链路、支付、库存、物流与售后状态。', path: '/mall/order-fulfillment/order-center' },
      { name: '物流履约工作台', access: '可操作', description: '筛选履约异常，执行发货并追加物流轨迹。', path: '/mall/order-fulfillment/logistics' },
      { name: '售后处理', access: '可操作', description: '审核订单项售后并执行符合条件的退款。', path: '/mall/after-sale-funds/after-sale' },
      { name: '会员列表', access: '可操作', description: '查询会员并维护允许修改的会员资料。', path: '/mall/member-operations/member' },
      { name: '对账与告警', access: '可操作', description: '处理资金差异、异常支付和对账告警。', path: '/mall/after-sale-funds/reconciliation' },
      { name: '售后资金处理中心', access: '可操作', description: '统一查看售后审核、退款、对账、告警和补偿工作项。', path: '/mall/after-sale-funds/center' }
    ]
  },
  {
    key: 'mall_product_ops',
    code: 'ROLE 02',
    name: '商品运营',
    objective: '维护可售商品信息与上架质量，关注库存但不直接履约。',
    writes: '分类、品牌、商品、上下架',
    forbidden: '不调整库存、不查询会员、不发货、不处理售后和退款',
    responsibilities: ['维护分类、品牌、SPU 与 SKU 信息', '控制商品上下架和前台展示质量', '通过商品库存中心关注缺货与低库存商品'],
    latestChange: '新增经营分析中心，只查看销售汇总、商品排行与商品库存聚合。',
    features: [
      { name: '经营分析中心', access: '只读', description: '查看销售汇总、成交趋势、商品排行与低库存聚合。', path: '/mall/overview/analytics' },
      { name: '商品库存中心', access: '只读', description: '查看商品状态、SKU 库存和库存风险。', path: '/mall/product-inventory/product-inventory-center' },
      { name: '分类管理', access: '可操作', description: '维护商城分类结构、排序和启停状态。', path: '/mall/product-inventory/category' },
      { name: '品牌管理', access: '可操作', description: '维护商品品牌资料和启停状态。', path: '/mall/product-inventory/brand' },
      { name: '商品管理', access: '可操作', description: '维护 SPU、SKU、图片、价格和上下架状态。', path: '/mall/product-inventory/product' },
      { name: '库存管理', access: '只读', description: '查看库存数量与流水，不执行库存调整。', path: '/mall/product-inventory/inventory' }
    ]
  },
  {
    key: 'mall_customer_service',
    code: 'ROLE 03',
    name: '客服售后',
    objective: '解决会员订单问题并完成售后审核，不接触资金执行。',
    writes: '会员资料、售后审核',
    forbidden: '不退款、不调库存、不发货、不追加物流轨迹',
    responsibilities: ['查询会员、订单和物流信息并回应咨询', '审核仅退款、退货退款及凭证材料', '跟踪售后进度并向会员解释审核结果'],
    latestChange: '新增经营分析中心，只查看会员复购聚合与售后工作量，不显示会员明细。',
    features: [
      { name: '经营分析中心', access: '只读', description: '查看成交会员、复购率和售后处理量等聚合指标。', path: '/mall/overview/analytics' },
      { name: '订单运营中心', access: '只读', description: '查询订单、支付、库存、履约和售后全链路。', path: '/mall/order-fulfillment/order-center' },
      { name: '物流履约工作台', access: '只读', description: '筛选物流状态、关注信号并查看完整轨迹。', path: '/mall/order-fulfillment/logistics' },
      { name: '售后处理', access: '可操作', description: '查询售后单、核对凭证并完成审核。', path: '/mall/after-sale-funds/after-sale' },
      { name: '售后资金处理中心', access: '只读', description: '聚合查看售后工作项并进入售后审核，资金异常工作项对本岗位隐藏。', path: '/mall/after-sale-funds/center' },
      { name: '会员列表', access: '可操作', description: '查询会员并维护允许修改的资料。', path: '/mall/member-operations/member' }
    ]
  },
  {
    key: 'mall_fulfillment',
    code: 'ROLE 04',
    name: '仓储履约',
    objective: '保证库存准确，并按订单完成发货和物流轨迹维护。',
    writes: '库存调整、发货、物流节点',
    forbidden: '不改会员、不改商品、不审售后、不执行退款',
    responsibilities: ['核对库存并处理入库、出库和预警阈值', '为已支付待发货订单创建唯一物流单', '只追加物流轨迹并处理运输异常和更正说明'],
    latestChange: '新增经营分析中心，只查看订单量、库存与物流履约聚合。',
    features: [
      { name: '经营分析中心', access: '只读', description: '查看订单量、可售库存、待发货、运输中和签收聚合。', path: '/mall/overview/analytics' },
      { name: '商品库存中心', access: '只读', description: '查看商品与 SKU 库存风险。', path: '/mall/product-inventory/product-inventory-center' },
      { name: '库存管理', access: '可操作', description: '调整库存、预警阈值并查看库存流水。', path: '/mall/product-inventory/inventory' },
      { name: '订单运营中心', access: '只读', description: '核对订单支付、库存和履约状态。', path: '/mall/order-fulfillment/order-center' },
      { name: '物流履约工作台', access: '可操作', description: '执行发货、追加轨迹并处理履约关注信号。', path: '/mall/order-fulfillment/logistics' }
    ]
  },
  {
    key: 'mall_finance_risk',
    code: 'ROLE 05',
    name: '财务风控',
    objective: '执行已审核退款并处理资金差异和告警。',
    writes: '售后退款、对账处理、告警确认',
    forbidden: '不审售后、不改商品和库存、不查询会员、不发货',
    responsibilities: ['复核已通过售后的退款执行条件', '执行退款并跟踪渠道处理结果', '处理支付差异、补偿任务、对账和资金告警'],
    latestChange: '新增经营分析中心，只查看销售、退款、净收入与资金异常聚合。',
    features: [
      { name: '经营分析中心', access: '只读', description: '查看销售额、退款额、净收入和未关闭资金异常。', path: '/mall/overview/analytics' },
      { name: '订单运营中心', access: '只读', description: '查询订单支付、退款和关联售后状态。', path: '/mall/order-fulfillment/order-center' },
      { name: '售后处理', access: '可操作', description: '查询已审核售后并执行退款，不修改审核结论。', path: '/mall/after-sale-funds/after-sale' },
      { name: '对账与告警', access: '可操作', description: '处理对账差异、异常支付和资金告警。', path: '/mall/after-sale-funds/reconciliation' },
      { name: '售后资金处理中心', access: '可操作', description: '集中处理退款、对账差异、异常告警和补偿任务。', path: '/mall/after-sale-funds/center' }
    ]
  }
]

export const permissionRows = [
  { capability: '经营分析中心（职责内聚合）', mall_ops_lead: read, mall_product_ops: read, mall_customer_service: read, mall_fulfillment: read, mall_finance_risk: read },
  { capability: '会员查询与资料维护', mall_ops_lead: full, mall_product_ops: none, mall_customer_service: full, mall_fulfillment: none, mall_finance_risk: none },
  { capability: '分类、品牌与商品维护', mall_ops_lead: full, mall_product_ops: full, mall_customer_service: none, mall_fulfillment: none, mall_finance_risk: none },
  { capability: '库存查询与调整', mall_ops_lead: full, mall_product_ops: read, mall_customer_service: none, mall_fulfillment: full, mall_finance_risk: none },
  { capability: '订单运营中心', mall_ops_lead: read, mall_product_ops: none, mall_customer_service: read, mall_fulfillment: read, mall_finance_risk: read },
  { capability: '物流履约查询与汇总', mall_ops_lead: full, mall_product_ops: none, mall_customer_service: read, mall_fulfillment: full, mall_finance_risk: none },
  { capability: '手工发货', mall_ops_lead: full, mall_product_ops: none, mall_customer_service: none, mall_fulfillment: full, mall_finance_risk: none },
  { capability: '追加物流轨迹与更正', mall_ops_lead: full, mall_product_ops: none, mall_customer_service: none, mall_fulfillment: full, mall_finance_risk: none },
  { capability: '售后查询与审核', mall_ops_lead: full, mall_product_ops: none, mall_customer_service: full, mall_fulfillment: none, mall_finance_risk: read },
  { capability: '售后退款执行', mall_ops_lead: full, mall_product_ops: none, mall_customer_service: none, mall_fulfillment: none, mall_finance_risk: full },
  { capability: '对账、差异与告警处理', mall_ops_lead: full, mall_product_ops: none, mall_customer_service: none, mall_fulfillment: none, mall_finance_risk: full },
  { capability: '售后资金与异常处理中心', mall_ops_lead: full, mall_product_ops: none, mall_customer_service: read, mall_fulfillment: none, mall_finance_risk: full }
]

export const operationsRoleKeys = roleDefinitions.map(role => role.key)

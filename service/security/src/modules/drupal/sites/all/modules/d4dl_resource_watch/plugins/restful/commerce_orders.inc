$plugin = array(
  'label' => t('Commerce Orders'),
  'resource' => 'commerce_order',
  'name' => 'commerce_orders',
  'entity_type' => 'commerce_order',
  'bundle' => 'commerce_order',
  'description' => t('Export the commerce order content type.'),
  'class' => 'RestfulCustomResource',
);
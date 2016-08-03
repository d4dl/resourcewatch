namespace Drupal\resource_watch\Plugin\resource;

/**
* Class Resource__1_0
* @package Drupal\resource_watch\Plugin\resource
*
* @Resource(
*   name = "watch:1.0",
*   resource = "commerce_order",
*   label = "Watch",
*   description = "My watched resources!",
*   authenticationTypes = TRUE,
*   authenticationOptional = TRUE,
*   dataProvider = {
*     "entityType": "node",
*     "bundles": {
*       "article"
*     },
*   },
*   majorVersion = 1,
*   minorVersion = 0
* )
*/
class Watch__1_0 extends ResourceEntity implements ResourceInterface {

/**
* Overrides EntityNode::publicFields().
*/
public function publicFields() {
$public_fields = parent::publicFields();

$public_fields['body'] = array(
'property' => 'body',
'sub_property' => 'value',
);

return $public_fields;
}
}
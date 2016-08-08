<?php

class D4DL_ResourceWatch_Model_Export
{
    const RESOURCE_WATCH_ENDPOINT = 'resourcewatch/settings/d4dl_resource_watch_endpoint';
    const RESOURCE_WATCH_PROCESS_KEY = 'resourcewatch/settings/d4dl_resource_watch_process_key';

    protected function _getHelper()
    {
        return Mage::helper('resourcewatch');
    }

    /**
     * Generates an XML file from the order data and places it into
     * the var/export directory
     *
     * @param Mage_Sales_Model_Order $order order object
     *
     * @return boolean
     */
    public function exportOrder($event) {
        $product = $event->getProduct();
        $order = $event->getOrder();
        $details = array(
            "event"=>$event,
            "product"=>$product,
            "order"=>$order
        );
        $typeDetails = array(
            "event"=>get_class($event),
            "order"=>get_class($order)
        );
        error_log("raw order: $order");
        error_log("Exporting order: " . json_encode($details, JSON_PRETTY_PRINT));
        error_log("Types are: " . json_encode($typeDetails, JSON_PRETTY_PRINT));


        $jsonOrder = Mage::helper('core')->jsonEncode($order);
        error_log("The mage order " . json_encode(json_decode($jsonOrder), JSON_PRETTY_PRINT));

        if(get_class($order) == 'Mage_Sales_Model_Order') {
            $orderDetails = array(
                    "transactionId"=>$order->getRealOrderId(),
                    "cartOrderSystemId"=>$order->getRealOrderId(),
                    'shoppingCartId' => 'demoMagentoDrupalStore',
                    'shoppingCartType' => 'magento_commerce_rest',
                    'shoppingCartName' => 'D4DL Magento Store',
                    'email' => $jsonOrder->customer_email,
                    "amount"=>$order->getTotalDue(),
                    'siteName'=>'Magento D4dl Store',
                    'tenantId'=>'magentoDemoClient',
                    'orderTag' => 'cart',
                    'processDefinitionKey' => Mage::getStoreConfig(self::RESOURCE_WATCH_PROCESS_KEY),
                    "status"=>$order->getStatusLabel()
            );
            $this->_getHelper()->_postOrderDetails(Mage::getStoreConfig(self::RESOURCE_WATCH_ENDPOINT), $orderDetails);
        } else {
            error_log("Ignoring order for class " . get_class($order));
        }
        return true;
    }
}

?>

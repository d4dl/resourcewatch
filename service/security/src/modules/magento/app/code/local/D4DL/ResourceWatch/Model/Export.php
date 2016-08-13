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
    public function exportOrder($event, $observerData) {
        $product = $event->getProduct();
        $order = $event->getOrder();
        // error_log("Debugging order: " . $event->getOrder()->debug());
        $details = array(
            "event"=>$event,
            "product"=>$product,
            "order"=>$order
        );
        $typeDetails = array(
            "event"=>get_class($event),
            "order"=>get_class($order)
        );

        $payment = $observerData['payment'];
        if(isset($payment)) {
            // $this->debugObject("Payent is set on the data array ", $payment);
            $this->debugJson("Order is set on the data array ", $payment->toJson());
            $orderPayment = $payment->getOrder();
        }
        $this->debug("Shipping carrier: " . $order->getShippingCarrier());
        $this->debug("Status : " . $order->getStatusLabel());
        $this->debug("Total Due : " . $order->getTotalDue());
        $this->debug("Customer : " . $order->getCustomerName());

        $jsonOrder = Mage::helper('core')->jsonEncode($order);
        if(isset($order)) {
            $jsonData = Mage::helper('core')->jsonEncode($order->getData());
            error_log("The mage DATA " . json_encode(json_decode($jsonData), JSON_PRETTY_PRINT), 3, '/usr/www/users/d4dl/remanplanet.com/magento/var/log/eventsd4dl.log');
        }
        error_log("The mage order " . json_encode(json_decode($jsonOrder), JSON_PRETTY_PRINT), 3, '/usr/www/users/d4dl/remanplanet.com/magento/var/log/eventsd4dl.log');


        if(get_class($order) == 'Mage_Sales_Model_Order') {
            $order = Mage::getModel('sales/order')->load($order->getId());
            error_log("Exporting order model: " . json_encode($details, JSON_PRETTY_PRINT), 3, '/usr/www/users/d4dl/remanplanet.com/magento/var/log/eventsd4dl.log');
            $shippingAddress = $order->getShippingAddress();
            $orderDetails = array(
                    "transactionId"=>$order->getRealOrderId(),
                    "cartOrderSystemId"=>$order->getRealOrderId(),
                    'shoppingCartId' => 'demoMagentoDrupalStore',
                    'shoppingCartType' => 'magento_commerce_rest',
                    'shoppingCartName' => 'D4DL Magento Store',
                    'email' => $jsonOrder->customer_email,
                    "amount"=>$order->getCustomerEmail(),
                    "shippingAddress"=>$shippingAddress->getData(),
                    "customerName"=>$order->getCustomerName(),
                    'siteName'=>'Magento D4dl Store',
                    'tenantId'=>'magentoDemoClient',
                    'orderTag' => 'cart',
                    'processDefinitionKey' => Mage::getStoreConfig(self::RESOURCE_WATCH_PROCESS_KEY),
                    "status"=>$order->getStatusLabel()
            );
            $this->_getHelper()->_postOrderDetails(Mage::getStoreConfig(self::RESOURCE_WATCH_ENDPOINT), $orderDetails);
        } else {
            error_log("Ignoring order for class " . get_class($order), 3, '/usr/www/users/d4dl/remanplanet.com/magento/var/log/eventsd4dl.log');
        }
        return true;
    }

    public function debug($message) {
        error_log($message, 3, '/usr/www/users/d4dl/remanplanet.com/magento/var/log/eventsd4dl.log');
    }

    public function debugJson($message, $json) {
        error_log($message . "\ndebugObject\n" . json_encode(json_decode($json), JSON_PRETTY_PRINT), 3, '/usr/www/users/d4dl/remanplanet.com/magento/var/log/eventsd4dl.log');
    }

    /**
     * @param $toDebug
     */
    public function debugObject($message, $toDebug)
    {
        ob_start();
        var_dump($toDebug);
        $contents = ob_get_contents();
        ob_end_clean();
        error_log($message . "\ndebugObject\n" . $contents, 3, '/usr/www/users/d4dl/remanplanet.com/magento/var/log/eventsd4dl.log');
    }
}

?>

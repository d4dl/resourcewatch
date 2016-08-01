<?php
/**
 */
class D4DL_ResourceWatch_Model_Observer
{
    /**
     * For finding the correct events to process: exploratory
     * @param Varien_Event_Observer $observer
     *   sales_order_place_after
     *   sales_order_invoice_pay
     *   sales_order_invoice_pay
     *   checkout_onepage_controller_success_action
     *   sales_order_payment_capture
     *   sales_order_invoice_register
     * https://magento2.atlassian.net/wiki/display/m1wiki/Magento+1.x+Events+Reference
     */
    public function processEvent(Varien_Event_Observer $observer) {
        try {
            $response =  $this->_getHelper()->checkSecretRequest($params);
            $product = $observer->getEvent()->getProduct();
            $order = $observer->getEvent()->getOrder();
            $event = $observer->getEvent();
            $details = array(
                "event"=>$event,
                "product"=>$product,
                "order"=>$order,
                "observer"=>$observer
            );
            Mage::log('D4DL Observer ');



            // Write a new line to var/log/product-updates.log
            $name = $product->getName();
            $sku = $product->getSku();
            Mage::log(
                "{$name} ({$sku}) updated",
                null,
                'product-updates.log'
            );

            Mage::getModel('d4dl_resourcewatch/export')->exportOrder($event);
        } catch (Exception $e) {
            try {
                Mage::log('Caught exception: ',  $e->getMessage(), "\n");
            } catch (Exception $e) {
                Mage::log('Caught exception handling exception:');
            }
        }
    }
    
}

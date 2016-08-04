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
            $event = $observer->getEvent();
            error_log("Observer data: " . " event " . $event->getName() . "\n" . json_encode($observer->getData(), JSON_PRETTY_PRINT));
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

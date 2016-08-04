<?php

class D4DL_ResourceWatch_Model_QueueOrder
{
    const ENDPOINT    = 'resourcewatch/settings/d4dl_resource_watch_endpoint';
    const PROCESS_KEY = 'resourcewatch/settings/d4dl_resource_watch_process_key';

    /**
     * @return boolean
     */
    public function setQueue($order, $status)
    {
        // Checkout line 340 of ipn.php  case Mage_Paypal_Model_Info::PAYMENTSTATUS_COMPLETED:
        try {
            $order->setData('state', $status);
            $order->setStatus($status);
            $history = $order->addStatusHistoryComment("Order was set to $status by simple rule: Is Paypal -> Is IPN Complete -> Is US/Canada.", false);
            $history->setIsCustomerNotified(false);
            $order->save();
        } catch (Exception $e) {
            Mage::log("Error queueing order " . $e->getMessage());
        }
        return true;
    }

}

?>

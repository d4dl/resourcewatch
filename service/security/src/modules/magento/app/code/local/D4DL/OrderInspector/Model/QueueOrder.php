<?php

class D4DL_OrderInspector_Model_QueueOrder
{

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
            Mage::log("Error queueing orer " . $e->getMessage());
        }
        return true;
    }

}

?>

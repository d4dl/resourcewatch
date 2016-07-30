<?php

class D4DL_OrderInspector_Model_Export
{

    /**
     * Generates an XML file from the order data and places it into
     * the var/export directory
     *
     * @param Mage_Sales_Model_Order $order order object
     *
     * @return boolean
     */
    public function exportOrder($order) {
        // Outbound report to Central Station not implemented
        $dirPath = Mage::getBaseDir('var') . DS . 'export';
        Mage::log("Export Order ... $dirPath");
        $exportPath = $dirPath . DS . $order->getIncrementId() . '.xml';
        Mage::log("Adding export: " . $exportPath);
        if($this->centralStationApproved($order)) {
            Mage::getModel('d4dl_orderinspector/queueorder')->queueuOrder($order, "okay_to_ship");
        }

        //if the export directory does not exist, create it
        if (!is_dir($dirPath)) {
            mkdir($dirPath, 0777, true);
        }

        $data = $order->getData();

        $xml = new SimpleXMLElement('<root/>');

        $callback =
            function ($value, $key) use (&$xml, &$callback) {
                if ($value instanceof Varien_Object && is_array($value->getData())) {
                    $value = $value->getData();
                }
                if (is_array($value)) {
                    array_walk_recursive($value, $callback);
                }
                $xml->addChild($key, serialize($value));
            };

        array_walk_recursive($data, $callback);

        file_put_contents(
            $exportPath,
            $xml->asXML()
        );

        return true;
    }

    public function centralStationApproved($order) {

        // Rule
        // Is it Magento
        //    Is it paypal, Is it IPN complete, Is it US/Canada

        // Other rules.
        // USPS? Address match? CVV match? Less thatn $150. Is CC?
        Mage::log("Not approving orders");

        return $this->orderIsPaypal($order) &&
            $this->orderIsIPNComplete($order) &&
            $this->orderIsFromWhitelistCountry($order);
    }


    public function orderIsPaypal($order) {
        return false;
    }
    
    public function orderIsIPNComplete($order) {
        // Before you make this approve orders 
        // make sure the order amount processed by paypal is actuall
        // the cost of the product.  ie they haven't hacked it.
        return false;
    }
    
    public function orderIsFromWhitelistCountry($order) {
        return false;
    }
}

?>

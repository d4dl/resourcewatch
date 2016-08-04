<?php
/**
 * Magento
 * @category    D4DL
 * @package     ResourceWatch
 * @copyright   Copyright (c) 2016 DeFord Logistics
 */
class D4DL_ResourceWatch_Helper_Data extends Mage_Payment_Helper_Data
{


    /**
     */
    public function _postOrderDetails($host, $parameter)
    {
        error_log("Magento client posting to $host: \n" . json_encode($parameter, JSON_PRETTY_PRINT));
        $client = new Varien_Http_Client();
        $client->setUri($host)
            ->setConfig(array('timeout' => 30))
            ->setHeaders('accept-encoding', 'application/json')
            ->setParameterPost($parameter)
            ->setMethod(Zend_Http_Client::POST);
        $request = $client->request();
        // Workaround for pseudo chunked messages which are yet too short, so
        // only an exception is is thrown instead of returning raw body
        if (!preg_match("/^([\da-fA-F]+)[^\r\n]*\r\n/sm", $request->getRawBody(), $m))
            return $request->getRawBody();

        return $request->getBody();
    }
}

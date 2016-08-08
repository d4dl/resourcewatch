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
    public function _postOrderDetails($host, $json)
    {
        $url = $host . "/cartOrders";
        error_log("Magento client posting to $url: \n" . json_encode($json, JSON_PRETTY_PRINT));
        $client = new Zend_Http_Client();
        $client->setUri($url)
            ->setConfig(array('timeout' => 30))
            ->setHeaders(array('Content-type: application/json', 'Authorization: Basic amRlZm9yZDpPcGVuU2VzYW1l', 'Accept-Encoding: application/json'));
        $client->setRawData(json_encode($json), 'application/json');
        $request = $client->request(Zend_Http_Client::POST);
        // Workaround for pseudo chunked messages which are yet too short, so
        // only an exception is is thrown instead of returning raw body
        if (!preg_match("/^([\da-fA-F]+)[^\r\n]*\r\n/sm", $request->getRawBody(), $m))
            return $request->getRawBody();

        return $request->getBody();
    }
}

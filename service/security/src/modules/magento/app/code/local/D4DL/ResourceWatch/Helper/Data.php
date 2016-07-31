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
     * Reading a page via HTTPS and returning its content.
     */
    protected function _getHttpsPage($host, $parameter)
    {
        $client = new Varien_Http_Client();
        $client->setUri($host)
            ->setConfig(array('timeout' => 30))
            ->setHeaders('accept-encoding', '')
            ->setParameterGet($parameter)
            ->setMethod(Zend_Http_Client::GET);
        $request = $client->request();
        // Workaround for pseudo chunked messages which are yet too short, so
        // only an exception is is thrown instead of returning raw body
        if (!preg_match("/^([\da-fA-F]+)[^\r\n]*\r\n/sm", $request->getRawBody(), $m))
            return $request->getRawBody();

        return $request->getBody();
    }
}

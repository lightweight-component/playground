package com.ajaxjs.net.netcard;

import static com.ajaxjs.net.netcard.GetCardInfo.FILTER_IPV4;
import static com.ajaxjs.net.netcard.GetCardInfo.addressesOfNoVirtualNICs;
import static com.ajaxjs.net.netcard.GetCardInfo.addressesOfPhysicalNICs;
import static com.ajaxjs.net.netcard.GetCardInfo.ipv4AddressesOfNoVirtualNICs;
import static com.ajaxjs.net.netcard.GetCardInfo.ipv4AddressesOfPhysicalNICs;

import java.net.NetworkInterface;
import java.net.SocketException;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestGetNetCardInfo {
	@Test
	public void testGetPhysicalNICs() {
		log.info("ipv4AddressesOfPhysicalNICs :{}", ipv4AddressesOfPhysicalNICs());
		log.info("addressesOfPhysicalNICs(FILTER_IPV4) nic:{}", addressesOfPhysicalNICs(FILTER_IPV4));
		log.info("ipv4AddressesOfNoVirtualNICs :{}", ipv4AddressesOfNoVirtualNICs());
		log.info("addressesOfNoVirtualNICs(FILTER_IPV4):{}", addressesOfNoVirtualNICs(FILTER_IPV4));
	}

	@Test
	void test() throws SocketException {
		for (NetworkInterface nic : GetCardInfo.getPhysicalNICs()) {
			System.out.println(nic.getName());
			System.out.println(nic.getDisplayName());
			System.out.println(GetCardInfo.formatMac(nic.getHardwareAddress(), "-"));
			System.out.println(nic.getInetAddresses());
			System.out.println(nic.getSubInterfaces());
			System.out.println("------------------------------------------------");
		}
	}
}

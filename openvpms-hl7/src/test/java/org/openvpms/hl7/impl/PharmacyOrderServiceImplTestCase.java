package org.openvpms.hl7.impl;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.hl7.PatientContext;
import org.openvpms.hl7.PharmacyOrderService;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Tests the {@link PharmacyOrderServiceImpl} class.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderServiceImplTestCase extends AbstractServiceTest {

    /**
     * The order service.
     */
    private PharmacyOrderService orderService;

    /**
     * The product to order.
     */
    private Product product;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        orderService = new PharmacyOrderServiceImpl(getArchetypeService(), LookupServiceHelper.getLookupService(),
                                                    getConnectors(), getManager());
        product = TestHelper.createProduct();
        product.setName("Valium 2mg");
        IMObjectBean productBean = new IMObjectBean(product);
        productBean.setValue("dispensingUnits", TestHelper.getLookup("lookup.uom", "TAB", "Tablets", true).getCode());
        productBean.setValue("sellingUnits", TestHelper.getLookup("lookup.uom", "BOX", "Box", true).getCode());
        productBean.setValue("dispInstructions", "Give 1 tablet once daily");
        product.setId(4001);
    }

    /**
     * Tests the {@link PharmacyOrderServiceImpl#createOrder(PatientContext, Product, BigDecimal, long, Date)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCreateOrder() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900+1000||RDE^O11^RDE_O11|1200022|P|2.5||||||UTF-8\r" +
                          "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "AL1|1||||Penicillin\r" +
                          "AL1|2||||Pollen\r" +
                          "ORC|NW|10231|||||||20140825090200+1000|2001^Blogs^Joe\r" +
                          "RXO|4001^Valium 2mg^OpenVPMS|||TAB^Tablets^OpenVPMS|||^Give 1 tablet once daily||||2|BOX^Box^OpenVPMS\r";

        Date date = getDatetime("2014-08-25 09:02:00").getTime();
        orderService.createOrder(getContext(), product, BigDecimal.valueOf(2), 10231, date);
        checkMessage(expected);
    }

    /**
     * Tests the {@link PharmacyOrderServiceImpl#updateOrder(PatientContext, Product, BigDecimal, long, Date)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testUpdateOrder() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900+1000||RDE^O11^RDE_O11|1200022|P|2.5||||||UTF-8\r" +
                          "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "AL1|1||||Penicillin\r" +
                          "AL1|2||||Pollen\r" +
                          "ORC|RP|10231|||||||20140825090200+1000|2001^Blogs^Joe\r" +
                          "RXO|4001^Valium 2mg^OpenVPMS|||TAB^Tablets^OpenVPMS|||^Give 1 tablet once daily||||2|BOX^Box^OpenVPMS\r";

        Date date = getDatetime("2014-08-25 09:02:00").getTime();
        orderService.updateOrder(getContext(), product, BigDecimal.valueOf(2), 10231, date);
        checkMessage(expected);
    }

    /**
     * Tests the {@link PharmacyOrderServiceImpl#updateOrder(PatientContext, Product, BigDecimal, long, Date)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCancelOrder() throws Exception {
        String expected = "MSH|^~\\&|VPMS|Main Clinic|Cubex|Cubex|20140825085900+1000||RDE^O11^RDE_O11|1200022|P|2.5||||||UTF-8\r" +
                          "PID|1||1001||Bar^Fido||20140701000000+1000|M|||123 Broadwater Avenue^^Cape Woolamai^VIC^3058||(03) 12345678|(03) 98765432|||||||||||||||||||||CANINE^Canine^OpenVPMS|KELPIE^Kelpie^OpenVPMS\r" +
                          "PV1|1|U|^^^Main Clinic||||||||||||||2001^Blogs^Joe||3001|||||||||||||||||||||||||20140825085500+1000\r" +
                          "AL1|1||||Penicillin\r" +
                          "AL1|2||||Pollen\r" +
                          "ORC|CA|10231|||||||20140825090200+1000|2001^Blogs^Joe\r" +
                          "RXO|4001^Valium 2mg^OpenVPMS|||TAB^Tablets^OpenVPMS|||^Give 1 tablet once daily||||2|BOX^Box^OpenVPMS\r";

        Date date = getDatetime("2014-08-25 09:02:00").getTime();
        orderService.cancelOrder(getContext(), product, BigDecimal.valueOf(2), 10231, date);
        checkMessage(expected);
    }
}

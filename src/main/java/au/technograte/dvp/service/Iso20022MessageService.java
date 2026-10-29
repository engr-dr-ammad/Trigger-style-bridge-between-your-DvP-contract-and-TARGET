package au.technograte.dvp.service;

import au.technograte.dvp.model.Payment;
import au.technograte.dvp.model.PaymentMessageType;
import au.technograte.dvp.model.Trade;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating ISO 20022 payment messages
 * Supports pacs.009 (FIToFI payments) for TARGET settlement
 */
@Service
public class Iso20022MessageService {

    private static final String ISO_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:pacs.009.003.02";
    private static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public String generatePacs009Message(Payment payment, Trade trade) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Root Document element
            Element document = doc.createElementNS(ISO_NAMESPACE, "Document");
            document.setAttribute("xmlns", ISO_NAMESPACE);
            doc.appendChild(document);

            // FIToFICustomerCreditTransferInitiation
            Element fiToFiPayment = doc.createElement("FIToFICstmrCdtTrfInitn");
            document.appendChild(fiToFiPayment);

            // Group Header
            Element groupHeader = doc.createElement("GrpHdr");
            fiToFiPayment.appendChild(groupHeader);

            addElement(doc, groupHeader, "MsgId", payment.getMessageId());
            addElement(doc, groupHeader, "CreDtTm", LocalDateTime.now().format(ISO_DATETIME));
            addElement(doc, groupHeader, "NbOfTxns", "1");

            // Payment Information
            Element paymentInfo = doc.createElement("PmtInf");
            fiToFiPayment.appendChild(paymentInfo);

            addElement(doc, paymentInfo, "PmtInfId", payment.getPaymentReference());
            addElement(doc, paymentInfo, "PmtMtd", "TRF");
            addElement(doc, paymentInfo, "ReqdExctnDt", LocalDateTime.now().format(ISO_DATETIME));

            // Debtor Bank
            Element dbtrAgt = doc.createElement("DbtrAgt");
            paymentInfo.appendChild(dbtrAgt);
            Element dbtrAgtId = doc.createElement("FinInstnId");
            dbtrAgt.appendChild(dbtrAgtId);
            addElement(doc, dbtrAgtId, "BIC", payment.getSenderBic());

            // Creditor Bank
            Element cdtrAgt = doc.createElement("CdtrAgt");
            paymentInfo.appendChild(cdtrAgt);
            Element cdtrAgtId = doc.createElement("FinInstnId");
            cdtrAgt.appendChild(cdtrAgtId);
            addElement(doc, cdtrAgtId, "BIC", payment.getReceiverBic());

            // Credit Transfer Transaction
            Element creditTransfer = doc.createElement("CdtTrfTxInf");
            paymentInfo.appendChild(creditTransfer);

            addElement(doc, creditTransfer, "PmtId", payment.getPaymentReference());

            // Amount
            Element amount = doc.createElement("Amt");
            creditTransfer.appendChild(amount);
            Element instructedAmount = doc.createElement("InstdAmt");
            amount.appendChild(instructedAmount);
            instructedAmount.setAttribute("Ccy", payment.getCurrency());
            instructedAmount.setTextContent(payment.getAmount().toPlainString());

            // Debtor
            Element debtor = doc.createElement("Dbtr");
            creditTransfer.appendChild(debtor);
            addElement(doc, debtor, "Nm", trade.getSeller());

            // Debtor Account
            Element dbtrAcct = doc.createElement("DbtrAcct");
            creditTransfer.appendChild(dbtrAcct);
            Element dbtrId = doc.createElement("Id");
            dbtrAcct.appendChild(dbtrId);
            addElement(doc, dbtrId, "IBAN", payment.getSenderAccount());

            // Creditor
            Element creditor = doc.createElement("Cdtr");
            creditTransfer.appendChild(creditor);
            addElement(doc, creditor, "Nm", trade.getBuyer());

            // Creditor Account
            Element cdtrAcct = doc.createElement("CdtrAcct");
            creditTransfer.appendChild(cdtrAcct);
            Element cdtrId = doc.createElement("Id");
            cdtrAcct.appendChild(cdtrId);
            addElement(doc, cdtrId, "IBAN", payment.getReceiverAccount());

            // Purpose - Reference to the bond/trade
            Element purpose = doc.createElement("Purp");
            creditTransfer.appendChild(purpose);
            addElement(doc, purpose, "Cd", "BOND");

            // Remittance Information
            Element remittance = doc.createElement("RmtInf");
            creditTransfer.appendChild(remittance);
            addElement(doc, remittance, "Ustrd", "Trade: " + trade.getTradeReference() + " Bond: " + trade.getBond().getTokenId());

            return documentToString(doc);
        } catch (Exception e) {
            throw new RuntimeException("Error generating ISO 20022 pacs.009 message: " + e.getMessage(), e);
        }
    }

    private void addElement(Document doc, Element parent, String tagName, String value) {
        Element element = doc.createElement(tagName);
        element.setTextContent(value);
        parent.appendChild(element);
    }

    private String documentToString(Document doc) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty("indent", "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);

        return writer.getBuffer().toString();
    }
}

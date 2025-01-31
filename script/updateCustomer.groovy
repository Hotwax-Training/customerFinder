import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityFind
import org.moqui.entity.EntityValue
import java.sql.Timestamp

ExecutionContext ec = context.ec
Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis())

EntityFind ef = ec.entity.find('mantle.party.FindCustomerView')
ef.selectFields()

if (emailAddress) {
    ef.condition("emailAddress", emailAddress)
}
EntityValue el = ef.one()

if (el != null) {
    EntityValue existingPartyContactMech = ec.entity.find('mantle.party.contact.PartyContactMech')
            .condition("contactMechId", el.contactMechId)
            .condition("contactMechPurposeId", "EmailPrimary")
            .one()

    if (existingPartyContactMech != null) {
        existingPartyContactMech.set('thruDate', currentTimestamp)
        existingPartyContactMech.update()
    }

    def newContactMech = ec.entity.makeValue('mantle.party.contact.ContactMech')
    newContactMech.setFields(context, true, null, null)
    newContactMech.setSequencedIdPrimary()
    newContactMech.contactMechTypeEnumId = "CmtEmailAddress"
    newContactMech.infoString = emailAddress
    newContactMech.create()

    def partyId = el.partyId

    def newPartyContactMech = ec.entity.makeValue('mantle.party.contact.PartyContactMech')
    newPartyContactMech.set('partyId', partyId)
    newPartyContactMech.set('contactMechId', newContactMech.contactMechId)
    newPartyContactMech.set('contactMechPurposeId', 'EmailPrimary')
    newPartyContactMech.set('fromDate', currentTimestamp)
    newPartyContactMech.create()

    if (postalAddress) {
        def postalAddressEntity = ec.entity.makeValue('mantle.party.contact.PostalAddress')
        postalAddressEntity.set('contactMechId', newContactMech.contactMechId)
        postalAddressEntity.set('address1', postalAddress)
        postalAddressEntity.set('city', city)
        postalAddressEntity.set('postalCode', postalCode)

        postalAddressEntity.create()
    }
    if (phoneNumber) {
        def telecomNumberEntity = ec.entity.makeValue('mantle.party.contact.TelecomNumber')
        telecomNumberEntity.set('contactMechId', newContactMech.contactMechId)
        telecomNumberEntity.set('contactNumber', phoneNumber)
        telecomNumberEntity.create()
    }

} else {
    throw new RuntimeException("Customer not found with email: ${emailAddress}")
}

return [partyId: partyId,message: "Customer record updated successfully."]




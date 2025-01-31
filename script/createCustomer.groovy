import org.moqui.context.ExecutionContext
import java.sql.Timestamp

//ec is the core Moqui object that provides access to services, entity and context.ec fetches
ExecutionContext ec=context.ec;

def serviceResult = ec.service.sync().name("PartyServices.find#Customer").parameters([emailAddress: email]).call()
if(serviceResult.partyIdList){
    return ec.message.addError("Email Already Exists")
}
// Get the current date
//Date date = new Date();
Timestamp date = new Timestamp(System.currentTimeMillis())

// Create Party
def newParty = ec.entity.makeValue("mantle.party.Party")
newParty.setSequencedIdPrimary()
        .set("partyTypeEnumId", "Person")
        .create()
def partyId = newParty.partyId

// Create Person
def person = ec.entity.makeValue("Person")
person.set("partyId", partyId)
person.set("firstName", firstName)
person.set("lastName", lastName)
person.create()

def contactMech = ec.entity.makeValue("ContactMech")
contactMech.setSequencedIdPrimary().set("contactMechTypeEnumId", "CmtEmailAddress")
contactMech.set("infoString", email).create()

def partyContactMech = ec.entity.makeValue("PartyContactMech")
partyContactMech.set("partyId", partyId)
partyContactMech.set("contactMechId",contactMech.contactMechId)
partyContactMech.set("contactMechPurposeId", "EmailPrimary")
partyContactMech.set("fromDate",date )
partyContactMech.create()

return [partyId: partyId, message: "Customer created successfully"]



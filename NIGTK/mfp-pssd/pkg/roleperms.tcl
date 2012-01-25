# ============================================================================
# Role: basic-user
# This role enables some basic access to Mediaflux functionality such as an account,
# access to DICOM doc types and some functions,  and simple asset access.
# It's really a layer underneath PSSD (it could go in its own package)
# ============================================================================
createRole basic-user
grantRoleReadWriteAccessDocTypes basic-user \
    { mf-dicom-patient mf-dicom-prefs mf-dicom-project mf-dicom-study mf-dicom-series \
      mf-name mf-note mf-user \
    }  
grantRoleReadAccessDocTypes basic-user  { mf-revision-history }
#
grantRoleReadAccessServices  basic-user \
   { actor.self.describe \
     asset.get asset.query asset.namespace.list asset.transcode \
     citeable.root.get citeable.id.exists  citeable.id.import \
     dicom.image.get dicom.metadata.get  \
     dictionary.entries.list  dictionary.entries.describe  \
     server.peer.exists server.identity server.uuid \
     sink.list transcode.describe   user.self.get  \
   }
grantRoleWriteAccessServices  basic-user { user.self.password.set dicom.ingest }
   
# Required by DaRIS portal. Should be somewhere else?
grantRoleReadAccessServices  basic-user { system.session.self.describe system.session.output.get }



# ============================================================================
# Role: federation-user
# We may wish to federate MF servers but independently of the PSSD data model 
# So we create a federation role at this lowest level and grant it to the
# basic-user
# ============================================================================
set federation_perms        { { service system.session.federate MODIFY } \
			      { service system.session.self.describe ACCESS } \
			      { service server.peer.describe ACCESS } \
			      { service server.peer.tag.list ACCESS } \
			      { service server.peer.status MODIFY } \
			      { service asset.exists ACCESS } \
			      { peer * ACCESS } }
createRole federation-user
grantRolePerms federation-user ${federation_perms} 

# ============================================================================
# role: dicom-ingest
# This role is created by the Mediaflux server (on install) and comes with some
# default permissions so don't destroy it. Then we add some new permissions.
# This role enables access for the default (non-PSSD) DICOM server
# Like basic-user, it's really a layer undeneath PSSD and could go
# in its own package if we needed to
# ============================================================================
set dicom_ingest_perms  {     { service  asset.exists          ACCESS } \
			      { service  asset.get             ACCESS } \
			      { service  asset.doc.type.exists ACCESS } \
			      { service  server.uuid           ACCESS } \
			      { service  mail.send             MODIFY } \
			      { service  notification.generate MODIFY } \
			      { document mf-dicom-patient      ACCESS } \
			      { document mf-dicom-subject      PUBLISH } \
			      { document mf-dicom-series       ACCESS } }
grantRolePerms dicom-ingest $dicom_ingest_perms

    
# ============================================================================
# Role: pssd.model.doc.user
# ============================================================================
createRole    pssd.model.doc.user
grantRoleReadWriteAccessDocTypes pssd.model.doc.user \
    { pssd-object    pssd-project        pssd-subject \
      pssd-ex-method pssd-study          pssd-dataset \
      pssd-transform pssd-acquisition    pssd-derivation \
      pssd-method    pssd-method-subject pssd-method-rsubject \
      pssd-notification pssd-project-harvest \
      pssd-role-member-registry \
      pssd-dicom-server-registry \
      hfi.pssd.dicom.series \
      hfi-bruker-study \
      hfi-bruker-series }

# ============================================================================
# Role: pssd.model.user
# ============================================================================
createRole    pssd.model.user
grantRoleReadAccessServices   pssd.model.user \
    { om.pssd.* \
      asset.model.* \
      actor.have \
      asset.doc.type.describe \
      asset.transcode.describe \
      asset.namespace.get \
      citeable.named.id.describe \
      dictionary.contains \
      user.exists user.describe \
      system.events.* \
      server.ping \
      system.logon \
      system.logoff \
      shopping.cart.* \
      service.background.describe }
grantRoleWriteAccessServices  pssd.model.user \
    { om.pssd.* \
      asset.create \
      asset.set \
      citeable.id.create \
	  service.background.abort \
	  shopping.cart.* \
      authorization.role.create }
grantRoleRole pssd.model.user pssd.model.doc.user
    
    
# Now grant the pssd.model.user access to  'basic-user' and 'federation-user'
# Although a user could be supplied the 'basic-user' role at the top level, and this
# would then give them this basic access, our only real context for delivering
# this role is via PSSD so we bundle it here.  If it was required, 'basic-user'
# could be split out into a 'basic-user' package (like the old 'essentials' was)
# and users could be given just this role.
# The hierarchy is user : {nig.pssd.model.user, pssd.model.user : basic user} but could be
# user : {nig.pssd.model.user, pssd.model.user, basic-user}
grantRoleRole pssd.model.user basic-user
grantRoleRole pssd.model.user federation-user

# ============================================================================
# Role: pssd.model.power.user
#
# These people have some extra rights to enable them to explore more of
# the system from aterm. Should be granted directly to a user
# ============================================================================
createRole    pssd.model.power.user
grantRolePerms   pssd.model.power.user \
{ { service dictionary.add MODIFY } \
  { service dictionary.destroy  MODIFY } \
  { service dictionary.entry.add MODIFY } \
  { service dictionary.entry.remove MODIFY } \
  { service asset.doc.type.create MODIFY } \
  { service asset.doc.type.destroy MODIFY } \
  { service server.log.display ADMINISTER } \
}

# ============================================================================
# Role: pssd.project.create
# ============================================================================
createRole pssd.project.create
grantRolePerms pssd.project.create \
    { { service authentication.user.exists ACCESS } \
      { service citeable.named.id.create   MODIFY } \
      { service user.authority.grant       ADMINISTER } }

# ============================================================================
# Role: pssd.subject.create
# ============================================================================
createRole pssd.subject.create
grantRolePerms pssd.subject.create \
    { { service citeable.named.id.create MODIFY } }

# ============================================================================
# Role: pssd.r-subject.admin
# ============================================================================
createRole pssd.r-subject.admin
grantRolePerms pssd.r-subject.admin \
    { { service citeable.named.id.create MODIFY } }

# ============================================================================
# Role: pssd.r-subject.guest
# ============================================================================
createRole pssd.r-subject.guest

# ============================================================================
# Role: pssd.object.admin
# ============================================================================
createRole pssd.object.admin
# grant pssd.object.admin role to system-administrator role 
# (so that the system administrators can access the meta data).
actor.grant :type role :name system-administrator :role -type role pssd.object.admin

# ============================================================================
# Role: pssd.object.guest
# ============================================================================
createRole pssd.object.guest

# ============================================================================
# Role: pssd.dicom-ingest
# ============================================================================
createRole pssd.dicom-ingest
grantRolePerms pssd.dicom-ingest \
    { { service asset.get ACCESS } \
      { service om.pssd.object.exists              ACCESS } \
      { service om.pssd.object.describe            ACCESS } \
      { service om.pssd.object.type                ACCESS } \
      { service om.pssd.object.destroy             MODIFY } \
      { service om.pssd.subject.create             MODIFY } \
      { service om.pssd.subject.update             MODIFY } \
      { service om.pssd.subject.method.find        ACCESS } \
      { service om.pssd.ex-method.study.step.find  ACCESS } \
      { service om.pssd.study.create               MODIFY } \
      { service om.pssd.study.update               MODIFY } \
      { service om.pssd.dataset.primary.create     MODIFY } \
      { service om.pssd.dataset.primary.update     MODIFY } \
      { service om.pssd.dataset.derivation.create  MODIFY } \
      { service om.pssd.dataset.derivation.update  MODIFY } \
      { service om.pssd.project.members.list       MODIFY } \
      { service om.pssd.role-member-registry.list  MODIFY } \
      { service user.describe                      ACCESS } \
      { service server.log                         MODIFY } \
      { service om.pssd.project.mail.send          ACCESS } \
      { service actor.have 			   ACCESS } \
      { service actor.describe			   ACCESS } \
      { service citeable.id.exists                 ACCESS } \
      { service citeable.id.import                 MODIFY } \
      { document pssd-role-member-registry         ACCESS } \
    }
grantRoleRole pssd.dicom-ingest pssd.model.doc.user

# THe DICOM server must have access to all PSSD objects
grantRoleRole pssd.dicom-ingest pssd.object.admin

# ============================================================================
# Parallel approach to user roles.  We will define this structure
# dicom-user : {nig.pssd.dicom-ingest, pssd.dicom-ingest : dicom-ingest} but could be
# user : {nig.pssd.dicom-ingest, pssd.dicom-ingest, dicom-ingest}
grantRoleRole pssd.dicom-ingest dicom-ingest

# ============================================================================
# Service: om.pssd.project.create
# ============================================================================
grantServicePerms om.pssd.project.create \
    { { service actor.self.grant ADMINISTER } \
      { service actor.grant      ADMINISTER } \
      { service user.grant      ADMINISTER }}
grantServiceRole  om.pssd.project.create service-user

# ============================================================================
# Service: om.pssd.project.update
# ============================================================================
grantServicePerms om.pssd.project.update \
    { { service actor.grant ADMINISTER } \
      { service actor.revoke ADMINISTER }
      { service user.grant ADMINISTER }
      { service user.revoke ADMINISTER }}
grantServiceRole  om.pssd.project.update service-user

# ============================================================================
# Service: om.pssd.project.members.replace
# ============================================================================
grantServicePerms om.pssd.project.members.replace \
    { { service actor.grant ADMINISTER } \
      { service actor.revoke ADMINISTER } \
      { service user.grant ADMINISTER } \
      { service user.revoke ADMINISTER } }
grantServiceRole  om.pssd.project.members.replace service-user


# ============================================================================
# Service: om.pssd.project.members.remove
# ============================================================================
grantServicePerms om.pssd.project.members.remove \
    { { service actor.revoke ADMINISTER } \
      { service user.revoke ADMINISTER }}
grantServiceRole  om.pssd.project.members.remove service-user

# ============================================================================
# Service: om.pssd.project.members.add
# ============================================================================
grantServicePerms om.pssd.project.members.add \
    { { service actor.grant ADMINISTER } \
      { service user.grant ADMINISTER }}
grantServiceRole  om.pssd.project.members.add service-user

# ============================================================================
# Service: om.pssd.project.members.list
# ============================================================================
grantServicePerms om.pssd.project.members.list \
    { { service actor.describe ACCESS } \
      { service user.describe ACCESS }}
grantServiceRole  om.pssd.project.members.list service-user


# ============================================================================
# Service: om.pssd.r-subject.create
# ============================================================================
grantServicePerms om.pssd.r-subject.create \
    { { service actor.grant ADMINISTER } }
grantServiceRole  om.pssd.r-subject.create service-user

# ============================================================================
# Service: om.pssd.subject.create
# ============================================================================
grantServiceRole  om.pssd.subject.create service-user

# ============================================================================
# Service: om.pssd.subject.update
# ============================================================================
grantServiceRole  om.pssd.subject.update service-user

# ============================================================================
# Service: om.pssd.study.create
# ============================================================================
grantServiceRole  om.pssd.study.create service-user

# ============================================================================
# Service: om.pssd.study.update
# ============================================================================
grantServiceRole  om.pssd.study.update service-user

# ============================================================================
# Service: om.pssd.dataset.primary.create
# ============================================================================
grantServiceRole  om.pssd.dataset.primary.create service-user

# ============================================================================
# Service: om.pssd.dataset.primary.update
# ============================================================================
grantServiceRole  om.pssd.dataset.primary.update service-user

# ============================================================================
# Service: om.pssd.dataset.derivation.create
# ============================================================================
grantServiceRole  om.pssd.dataset.derivation.create service-user

# ============================================================================
# Service: om.pssd.dataset.derivation.update
# ============================================================================
grantServiceRole  om.pssd.dataset.derivation.update service-user

# ============================================================================
# Service: om.pssd.object.describe
# ============================================================================
grantServiceRole  om.pssd.object.describe pssd.r-subject.guest
grantServicePerms om.pssd.object.describe { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.object.lock
# ============================================================================
grantServiceRole  om.pssd.object.lock service-user

# ============================================================================
# Service: om.pssd.object.unlock
# ============================================================================
grantServiceRole  om.pssd.object.unlock service-user

# ============================================================================
# Service: om.pssd.object.session.lock
# ============================================================================
grantServiceRole  om.pssd.object.session.lock service-user

# ============================================================================
# Service: om.pssd.object.session.unlock
# ============================================================================
grantServiceRole  om.pssd.object.session.unlock service-user

# ============================================================================
# Service: om.pssd.object.exists
# ============================================================================
grantServiceRole om.pssd.object.exists pssd.object.admin

# ============================================================================
# Service: om.pssd.object.type
# ============================================================================
grantServicePerms om.pssd.object.type { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.object.attach
# ============================================================================
grantServiceRole  om.pssd.object.attach service-user

# ============================================================================
# Service: om.pssd.object.attachment.get
# ============================================================================
grantServicePerms om.pssd.object.attachment.get { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.object.deattach
# ============================================================================
grantServiceRole  om.pssd.object.detach service-user

# ============================================================================
# Service: om.pssd.object.update
# ============================================================================
grantServiceRole  om.pssd.object.update service-user

# ============================================================================
# Service: om.pssd.object.destroy
# ============================================================================
grantServiceRole  om.pssd.object.destroy service-user

# ============================================================================
# Service: om.pssd.object.find
# ============================================================================
grantServicePerms om.pssd.object.find { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.object.icon.get
# ============================================================================
grantServicePerms om.pssd.object.icon.get { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.user.describe
# ============================================================================
grantServicePerms om.pssd.user.describe { { service * ACCESS } }

# ============================================================================
# Service: om.pssd.collection.members
# ============================================================================
grantServicePerms om.pssd.collection.members { { service * ACCESS } }

# ============================================================================
#
# SHOPPINGCART SERVICES
#
# ============================================================================
grantServiceRole om.pssd.shoppingcart.content.add service-user
grantServiceRole om.pssd.shoppingcart.content.clear service-user
grantServiceRole om.pssd.shoppingcart.content.list  service-user
grantServiceRole om.pssd.shoppingcart.content.remove service-user
grantServiceRole om.pssd.shoppingcart.create service-user
grantServiceRole om.pssd.shoppingcart.describe  service-user
grantServiceRole om.pssd.shoppingcart.destination.list service-user
grantServiceRole om.pssd.shoppingcart.destroy  service-user
grantServiceRole om.pssd.shoppingcart.list  service-user
grantServiceRole om.pssd.shoppingcart.order  service-user
grantServiceRole om.pssd.shoppingcart.output.retrieve  service-user
grantServiceRole om.pssd.shoppingcart.template.create   service-user
grantServiceRole om.pssd.shoppingcart.template.destroy  service-user
grantServiceRole om.pssd.shoppingcart.update  service-user


# ============================================================================
# Service: om.pssd.dicom.*
# ============================================================================
grantServiceRole  om.pssd.dicom.ae.list service-user
grantServiceRole  om.pssd.dicom.send service-user
grantServiceRole  om.pssd.dicom.dataset.count service-user


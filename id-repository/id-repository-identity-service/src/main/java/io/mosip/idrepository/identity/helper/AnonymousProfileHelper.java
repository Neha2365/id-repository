package io.mosip.idrepository.identity.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.idrepository.core.builder.IdentityIssuanceProfileBuilder;
import io.mosip.idrepository.core.dto.DocumentsDTO;
import io.mosip.idrepository.core.dto.IdentityIssuanceProfile;
import io.mosip.idrepository.core.dto.IdentityMapping;
import io.mosip.idrepository.core.logger.IdRepoLogger;
import io.mosip.idrepository.core.security.IdRepoSecurityManager;
import io.mosip.idrepository.core.util.EnvUtil;
import io.mosip.idrepository.identity.entity.AnonymousProfileDto;
import io.mosip.idrepository.identity.entity.AnonymousProfileEntity;
import io.mosip.idrepository.identity.repository.AnonymousProfileRepo;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.retry.WithRetry;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.UUIDUtils;

@Component
@Transactional
public class AnonymousProfileHelper {
	
	Logger mosipLogger = IdRepoLogger.getLogger(AnonymousProfileHelper.class);

	@Autowired
	private AnonymousProfileRepo anonymousProfileRepo;

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private ObjectStoreHelper objectStoreHelper;
	
	@Autowired
	private ChannelInfoHelper channelInfoHelper;

	@Value("${mosip.identity.mapping-file}")
	private String identityMappingJson;
	
	@Autowired
	private AnonymousProfileDto anonymousProfileDto;
	
//	private byte[] oldUinData;
//
//	private byte[] newUinData;
//	
//	private String regId;
//
	 String oldCbeff;
//
     String newCbeff;
//	
//	private String uinHash;
//	
//	private String oldCbeffRefId;
//	
//	private String newCbeffRefId;
	
	@PostConstruct
	public void init() throws IOException {
		try (InputStream xsdBytes = new URL(identityMappingJson).openStream()) {
			IdentityMapping identityMapping = mapper.readValue(IOUtils.toString(xsdBytes, StandardCharsets.UTF_8),
					IdentityMapping.class);
			IdentityIssuanceProfileBuilder.setIdentityMapping(identityMapping);
		}
		IdentityIssuanceProfileBuilder.setDateFormat(EnvUtil.getIovDateFormat());
	}

	@Async("anonymousProfileExecutor")
	public void buildAndsaveProfile(boolean isDraft) {
		if (!isDraft)
			try {
				List<DocumentsDTO> oldDocList = List.of(new DocumentsDTO());
				List<DocumentsDTO> newDocList = List.of(new DocumentsDTO());
				if (Objects.isNull(anonymousProfileDto.getOldCbeff()) && Objects.nonNull(anonymousProfileDto.getOldCbeffRefId()))
					oldCbeff = CryptoUtil
							.encodeToURLSafeBase64(objectStoreHelper.getBiometricObject(anonymousProfileDto.getUinHash(), anonymousProfileDto.getOldCbeffRefId()));
				anonymousProfileDto.setOldCbeff(oldCbeff);
				if (Objects.isNull(anonymousProfileDto.getNewCbeff()) && Objects.nonNull(anonymousProfileDto.getNewCbeffRefId()))
					newCbeff = CryptoUtil
							.encodeToURLSafeBase64(objectStoreHelper.getBiometricObject(anonymousProfileDto.getUinHash(), anonymousProfileDto.getNewCbeffRefId()));
				anonymousProfileDto.setNewCbeff(newCbeff);
				if (Objects.nonNull(oldCbeff))
					oldDocList = List.of(new DocumentsDTO(IdentityIssuanceProfileBuilder.getIdentityMapping()
							.getIdentity().getIndividualBiometrics().getValue(), oldCbeff));
				if (Objects.nonNull(newCbeff))
					newDocList = List.of(new DocumentsDTO(IdentityIssuanceProfileBuilder.getIdentityMapping()
							.getIdentity().getIndividualBiometrics().getValue(), newCbeff));
				String id = UUIDUtils.getUUID(UUIDUtils.NAMESPACE_OID, anonymousProfileDto.getRegId()).toString();
				IdentityIssuanceProfile profile = IdentityIssuanceProfile.builder()
						.setFilterLanguage(EnvUtil.getAnonymousProfileFilterLanguage())
						.setProcessName(Objects.isNull(anonymousProfileDto.getOldUinData()) ? "New" : "Update").setOldIdentity(anonymousProfileDto.getOldUinData())
						.setOldDocuments(oldDocList).setNewIdentity(anonymousProfileDto.getNewUinData()).setNewDocuments(newDocList).build();
				AnonymousProfileEntity anonymousProfile = AnonymousProfileEntity.builder().id(id)
						.profile(mapper.writeValueAsString(profile)).createdBy(IdRepoSecurityManager.getUser())
						.crDTimes(DateUtils.getUTCCurrentDateTime()).build();
				anonymousProfileRepo.save(anonymousProfile);
				updateChannelInfo();
			} catch (Exception e) {
				mosipLogger.warn(IdRepoSecurityManager.getUser(), "AnonymousProfileHelper", "buildAndsaveProfile",
						ExceptionUtils.getStackTrace(e));
			}
	}

	@WithRetry
	public void updateChannelInfo() {
		channelInfoHelper.updatePhoneChannelInfo(anonymousProfileDto.getOldUinData(), anonymousProfileDto.getNewUinData());
		channelInfoHelper.updateEmailChannelInfo(anonymousProfileDto.getOldUinData(), anonymousProfileDto.getNewUinData());
	}



}
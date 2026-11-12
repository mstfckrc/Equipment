package com.equipmenthub.rentalservice.repository;

import com.equipmenthub.rentalservice.entity.EquipmentUnitStatus;

import java.util.UUID;

public interface EquipmentUnitCount {

	UUID getInventoryId();

	EquipmentUnitStatus getStatus();

	long getUnitCount();

}

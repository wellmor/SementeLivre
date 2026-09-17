package com.sementelivre.backend.repository;

import com.sementelivre.backend.entity.Admin;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdminRepository extends BaseRepository<Admin, UUID> {
}

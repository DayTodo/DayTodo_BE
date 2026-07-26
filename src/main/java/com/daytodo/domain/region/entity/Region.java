package com.daytodo.domain.region.entity;

import com.daytodo.domain.region.enums.RegionLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "region")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "region_id")
    private Long regionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Region parent;

    @Column (name = "region_name", nullable = false, length = 50)
    private String regionName;

    @Enumerated(EnumType.STRING)
    @Column (name = "region_level", nullable = false, length = 20)
    private RegionLevel regionLevel;

    public Region(Region parent, String regionName, RegionLevel regionLevel) {
        this.parent = parent;
        this.regionName = regionName;
        this.regionLevel = regionLevel;
    }
}

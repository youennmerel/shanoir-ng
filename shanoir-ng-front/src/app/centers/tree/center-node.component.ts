/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { AcquisitionEquipmentPipe } from '../../acquisition-equipments/shared/acquisition-equipment.pipe';

import { Selection } from 'src/app/studies/study/study-tree.component';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { AcquisitionEquipmentNode, CenterNode } from '../../tree/tree.model';
import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';


@Component({
    selector: 'center-node',
    templateUrl: 'center-node.component.html'
})

export class CenterNodeComponent implements OnChanges {

    @Input() input: CenterNode | Center;
    @Output() selectedChange: EventEmitter<void> = new EventEmitter();
    @Output() onEquipementNodeSelect: EventEmitter<number> = new EventEmitter();
    @Output() onNodeSelect: EventEmitter<number> = new EventEmitter();
    node: CenterNode;
    loading: boolean = false;
    menuOpened: boolean = false;
    detailsPath: string = '/center/details/';
    @Input() selection: Selection = new Selection();
    @Input() withMenu: boolean = true;

    constructor(
        private centerService: CenterService,
        private acquisitionEquipmentPipe: AcquisitionEquipmentPipe,
        private keycloakService: KeycloakService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof CenterNode) {
                this.node = this.input;
            } else {
                throw new Error('not implemented yet');
            }
        }
    }

    hasChildren(): boolean | 'unknown' {
        if (!this.node.acquisitionEquipments) return false;
        else if (this.node.acquisitionEquipments == 'UNLOADED') return 'unknown';
        else return this.node.acquisitionEquipments.length > 0;
    }

    loadEquipments() {
        this.loading = true;
        this.centerService.get(this.node.id).then(
            center =>  {
                if (center) {
                    this.node.acquisitionEquipments = center.acquisitionEquipments.map(
                            acqEq => new AcquisitionEquipmentNode(this.node, acqEq.id, this.acquisitionEquipmentPipe.transform(acqEq), 'UNLOADED', this.keycloakService.isUserAdminOrExpert()));
                }
                this.loading = false;
                this.node.open();
            }).catch(() => {
                this.loading = false;
            });
    }

    onEquipmentDelete(index: number) {
        (this.node.acquisitionEquipments as AcquisitionEquipmentNode[]).splice(index, 1) ;
    }
}

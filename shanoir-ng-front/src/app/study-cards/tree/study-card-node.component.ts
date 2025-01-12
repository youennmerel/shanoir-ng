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
import { Router } from '@angular/router';

import { StudyCardNode } from '../../tree/tree.model';
import { StudyCard } from '../shared/study-card.model';
import {StudyCardService} from "../shared/study-card.service";


@Component({
    selector: 'studycard-node',
    templateUrl: 'study-card-node.component.html'
})

export class StudyCardNodeComponent implements OnChanges {

    @Input() input: StudyCardNode | StudyCard;
    @Output() selectedChange: EventEmitter<void> = new EventEmitter();
    @Output() onCardDelete: EventEmitter<void> = new EventEmitter();
    node: StudyCardNode;
    loading: boolean = false;
    menuOpened: boolean = false;
    detailsPath: string = '/study-card/details/';

    constructor(
        private router: Router,
        private cardService: StudyCardService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof StudyCardNode) {
                this.node = this.input;
            } else {
                throw new Error('not implemented yet');
            }
        }
    }

    deleteStudyCard() {
        this.cardService.get(this.node.id).then(entity => {
            this.cardService.deleteWithConfirmDialog(this.node.title, entity).then(deleted => {
                if (deleted) {
                    this.onCardDelete.emit();
                }
            });
        })
    }
}

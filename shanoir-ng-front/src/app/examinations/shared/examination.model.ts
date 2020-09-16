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

import { Entity } from "../../shared/components/entity/entity.abstract";
import { IdName } from "../../shared/models/id-name.model";
import { SubjectWithSubjectStudy } from "../../subjects/shared/subject.with.subject-study.model";
import { Center } from '../../centers/shared/center.model';
import { Study } from '../../studies/shared/study.model';
import { Subject } from '../../subjects/shared/subject.model';
import { ServiceLocator } from '../../utils/locator.service';
import { ExaminationService } from './examination.service';
import { ExaminationDTO } from './examination.dto';
import { InstrumentBasedAssessment } from "../instrument-assessment/instrument.model"

export class Examination extends Entity {
    id: number;
    examinationDate: Date;
    subject: IdName | Subject;
    study: IdName | Study;
    center: IdName | Center;
    examinationExecutive: IdName;
    subjectStudy: SubjectWithSubjectStudy;
    comment: string;
    note: string;
    subjectWeight: number;
    instrumentBasedAssessmentList: InstrumentBasedAssessment[];
    extraDataFilePathList: string[] = [];
    preclinical: boolean;
    hasStudyCenterData: boolean = false;  
    service: ExaminationService = ServiceLocator.injector.get(ExaminationService);

    // Override
    public stringify() {
        return JSON.stringify(new ExaminationDTO(this), this.replacer);
    }
}
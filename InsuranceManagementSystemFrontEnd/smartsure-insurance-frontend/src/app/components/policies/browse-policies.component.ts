import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { PolicyService } from '../../services/policy.service';
import { PolicyProduct } from '../../models';

const travelDateValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const start = group.get('travelStartDate')?.value;
  const end = group.get('travelEndDate')?.value;
  if (!start || !end) {
    return null;
  }

  return new Date(end) > new Date(start) ? null : { travelDateRange: true };
};

@Component({
  selector: 'app-browse-policies',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './browse-policies.component.html',
})
export class BrowsePoliciesComponent implements OnInit {
  products: PolicyProduct[] = [];
  loading = true;
  purchaseLoading = false;
  selectedProduct: PolicyProduct | null = null;
  showModal = false;
  success = '';
  error = '';
  purchaseBlockedMessage = '';
  purchaseForm: FormGroup;
  readonly currentYear = new Date().getFullYear();
  readonly today = new Date().toISOString().split('T')[0];

  constructor(
    private policyService: PolicyService,
    private fb: FormBuilder,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.purchaseForm = this.fb.group({
      age: ['', Validators.required],
      gender: [''],
      preExistingCondition: [''],
      membersToCover: [''],
      smoker: [''],
      nomineeName: [''],
      nomineeRelationship: [''],
      nomineeAge: [''],
      vehicleType: [''],
      registrationNumber: [''],
      makeModel: [''],
      yearOfManufacture: [''],
      hasValidDrivingLicence: [''],
      propertyType: [''],
      propertyAddress: [''],
      propertyAge: [''],
      ownershipStatus: [''],
      destinationType: [''],
      travelStartDate: [''],
      travelEndDate: [''],
      travellersCount: [''],
      travelPurpose: [''],
    }, { validators: travelDateValidator });
  }

  ngOnInit(): void {
    this.policyService.getAllProducts().subscribe({
      next: (res) => {
        this.products = res.data || [];
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to load plans.';
      }
    });
  }

  openPurchase(product: PolicyProduct): void {
    this.purchaseBlockedMessage = '';
    this.error = '';

    this.policyService.getMyPolicies().subscribe({
      next: (res) => {
        const existingPolicies = res.data || [];
        const alreadyActive = existingPolicies.some(policy =>
          policy.status === 'ACTIVE' && policy.policyType === product.type
        );

        if (alreadyActive) {
          this.purchaseBlockedMessage = `You already have an active ${product.type} policy. You can purchase a new one after your current policy expires.`;
          this.toastr.error(this.purchaseBlockedMessage);
          return;
        }

        this.selectedProduct = product;
        this.purchaseForm.reset();
        this.applyTypeSpecificValidators();
        this.showModal = true;
      },
      error: (err) => {
        this.purchaseBlockedMessage = err.error?.message || 'Unable to verify your existing policies right now.';
        this.toastr.error(this.purchaseBlockedMessage);
      }
    });
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedProduct = null;
    this.purchaseForm.reset();
  }

  purchase(): void {
    if (!this.selectedProduct) {
      return;
    }

    if (this.purchaseForm.invalid || this.invalidDrivingLicenceSelected) {
      this.purchaseForm.markAllAsTouched();
      return;
    }

    this.purchaseLoading = true;
    this.error = '';

    const payload = {
      productId: this.selectedProduct.id,
      age: Number(this.purchaseForm.get('age')?.value),
      extraDetails: this.buildExtraDetails()
    };

    this.policyService.purchasePolicy(payload).subscribe({
      next: (res) => {
        this.success = `Policy purchased! Number: ${res.data?.policyNumber}`;
        this.purchaseLoading = false;
        this.closeModal();
        this.toastr.success(this.success);
        this.router.navigate(['/customer/policies/my-policies']);
      },
      error: (err) => {
        this.error = err.error?.message || 'Purchase failed';
        this.toastr.error(this.error);
        this.purchaseLoading = false;
      }
    });
  }

  coverageTypeColor(type: string): string {
    const map: Record<string, string> = {
      HEALTH: 'text-emerald-400 bg-emerald-400/10 border-emerald-400/20',
      LIFE: 'text-blue-400 bg-blue-400/10 border-blue-400/20',
      VEHICLE: 'text-orange-400 bg-orange-400/10 border-orange-400/20',
      PROPERTY: 'text-purple-400 bg-purple-400/10 border-purple-400/20',
      TRAVEL: 'text-cyan-400 bg-cyan-400/10 border-cyan-400/20',
    };
    return map[type?.toUpperCase()] || 'text-slate-400 bg-slate-400/10 border-slate-400/20';
  }

  isType(type: string): boolean {
    return this.selectedProduct?.type === type;
  }

  get invalidDrivingLicenceSelected(): boolean {
    return this.purchaseForm.get('hasValidDrivingLicence')?.value === 'No';
  }

  get showPreExistingInfo(): boolean {
    const condition = this.purchaseForm.get('preExistingCondition')?.value;
    return !!condition && condition !== 'None';
  }

  get showSmokerInfo(): boolean {
    return this.purchaseForm.get('smoker')?.value === 'Yes';
  }

  get showOldVehicleInfo(): boolean {
    const year = Number(this.purchaseForm.get('yearOfManufacture')?.value);
    return !!year && this.currentYear - year > 10;
  }

  get showOldPropertyInfo(): boolean {
    return Number(this.purchaseForm.get('propertyAge')?.value) > 50;
  }

  get showTenantInfo(): boolean {
    return this.purchaseForm.get('ownershipStatus')?.value === 'Tenant';
  }

  get ageRangeMessage(): string {
    if (!this.selectedProduct) {
      return '';
    }

    return `This policy is available for age ${this.selectedProduct.minAge} to ${this.selectedProduct.maxAge} only.`;
  }

  private buildExtraDetails(): Record<string, unknown> {
    const age = Number(this.purchaseForm.get('age')?.value);

    if (this.isType('HEALTH')) {
      return {
        age,
        gender: this.purchaseForm.get('gender')?.value,
        preExistingMedicalConditions: this.purchaseForm.get('preExistingCondition')?.value,
        membersToCover: this.purchaseForm.get('membersToCover')?.value,
      };
    }

    if (this.isType('LIFE')) {
      return {
        age,
        gender: this.purchaseForm.get('gender')?.value,
        smoker: this.purchaseForm.get('smoker')?.value,
        nomineeDetails: {
          name: this.purchaseForm.get('nomineeName')?.value,
          relationship: this.purchaseForm.get('nomineeRelationship')?.value,
          age: Number(this.purchaseForm.get('nomineeAge')?.value),
        }
      };
    }

    if (this.isType('VEHICLE')) {
      return {
        age,
        vehicleDetails: {
          vehicleType: this.purchaseForm.get('vehicleType')?.value,
          registrationNumber: this.purchaseForm.get('registrationNumber')?.value,
          makeAndModel: this.purchaseForm.get('makeModel')?.value,
          yearOfManufacture: Number(this.purchaseForm.get('yearOfManufacture')?.value),
          hasValidDrivingLicence: this.purchaseForm.get('hasValidDrivingLicence')?.value,
        }
      };
    }

    if (this.isType('PROPERTY')) {
      return {
        age,
        propertyDetails: {
          propertyType: this.purchaseForm.get('propertyType')?.value,
          propertyAddress: this.purchaseForm.get('propertyAddress')?.value,
          propertyAge: Number(this.purchaseForm.get('propertyAge')?.value),
          ownershipStatus: this.purchaseForm.get('ownershipStatus')?.value,
        }
      };
    }

      return {
        age,
        travelDetails: {
          destinationType: this.purchaseForm.get('destinationType')?.value,
          travelStartDate: this.purchaseForm.get('travelStartDate')?.value,
          travelEndDate: this.purchaseForm.get('travelEndDate')?.value,
          numberOfTravellers: Number(this.purchaseForm.get('travellersCount')?.value),
          purposeOfTravel: this.purchaseForm.get('travelPurpose')?.value,
        }
      };
    }

  private applyTypeSpecificValidators(): void {
    if (!this.selectedProduct) {
      return;
    }

    this.clearOptionalValidators();

    this.purchaseForm.get('age')?.setValidators([
      Validators.required,
      Validators.min(this.selectedProduct.minAge),
      Validators.max(this.selectedProduct.maxAge),
    ]);

    if (this.isType('HEALTH')) {
      this.purchaseForm.get('gender')?.setValidators([Validators.required]);
      this.purchaseForm.get('preExistingCondition')?.setValidators([Validators.required]);
      this.purchaseForm.get('membersToCover')?.setValidators([Validators.required]);
    }

    if (this.isType('LIFE')) {
      this.purchaseForm.get('gender')?.setValidators([Validators.required]);
      this.purchaseForm.get('smoker')?.setValidators([Validators.required]);
      this.purchaseForm.get('nomineeName')?.setValidators([Validators.required]);
      this.purchaseForm.get('nomineeRelationship')?.setValidators([Validators.required]);
      this.purchaseForm.get('nomineeAge')?.setValidators([Validators.required, Validators.min(1), Validators.max(100)]);
    }

    if (this.isType('VEHICLE')) {
      this.purchaseForm.get('vehicleType')?.setValidators([Validators.required]);
      this.purchaseForm.get('registrationNumber')?.setValidators([Validators.required, Validators.minLength(6)]);
      this.purchaseForm.get('makeModel')?.setValidators([Validators.required]);
      this.purchaseForm.get('yearOfManufacture')?.setValidators([Validators.required, Validators.min(1990), Validators.max(this.currentYear)]);
      this.purchaseForm.get('hasValidDrivingLicence')?.setValidators([Validators.required]);
    }

    if (this.isType('PROPERTY')) {
      this.purchaseForm.get('propertyType')?.setValidators([Validators.required]);
      this.purchaseForm.get('propertyAddress')?.setValidators([Validators.required]);
      this.purchaseForm.get('propertyAge')?.setValidators([Validators.required, Validators.min(0), Validators.max(100)]);
      this.purchaseForm.get('ownershipStatus')?.setValidators([Validators.required]);
    }

    if (this.isType('TRAVEL')) {
      this.purchaseForm.get('destinationType')?.setValidators([Validators.required]);
      this.purchaseForm.get('travelStartDate')?.setValidators([Validators.required]);
      this.purchaseForm.get('travelEndDate')?.setValidators([Validators.required]);
      this.purchaseForm.get('travellersCount')?.setValidators([Validators.required, Validators.min(1), Validators.max(10)]);
      this.purchaseForm.get('travelPurpose')?.setValidators([Validators.required]);
    }

    Object.values(this.purchaseForm.controls).forEach(control => control.updateValueAndValidity());
  }

  private clearOptionalValidators(): void {
    const optionalControls = [
      'gender', 'preExistingCondition', 'membersToCover', 'smoker',
      'nomineeName', 'nomineeRelationship', 'nomineeAge',
      'vehicleType', 'registrationNumber', 'makeModel', 'yearOfManufacture', 'hasValidDrivingLicence',
      'propertyType', 'propertyAddress', 'propertyAge', 'ownershipStatus',
      'destinationType', 'travelStartDate', 'travelEndDate', 'travellersCount', 'travelPurpose'
    ];

    optionalControls.forEach(name => {
      this.purchaseForm.get(name)?.clearValidators();
      this.purchaseForm.get(name)?.updateValueAndValidity({ emitEvent: false });
    });
  }
}

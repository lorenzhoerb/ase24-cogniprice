export interface SimplePricingRule {
  id: number;                 // Unique identifier of the pricing rule
  name: string;               // Name of the pricing rule
  active: boolean;            // Indicates if the rule is active
  positionMatchType: MatchType;  // Match type for the position
  positionReference: PositionReference;
  positionValue: number;      // Value of the position
  positionUnit: Unit;         // Unit for the position value
  scope: Scope;               // Scope of the pricing rule
  hasMaxLimit: boolean;       // Indicates if the rule has a maximum limit
  hasMinLimit: boolean;       // Indicates if the rule has a minimum limit
}

export interface PricingRuleDetails {
  id: number;
  name: string;
  position: Position;
  scope: Scope;
  appliedCategories: string[]
  appliedProductIds: number[]
  minLimit?: PriceLimit;
  maxLimit?: PriceLimit;
  active: boolean;
}

export interface PricingRuleRequest {
  name: string;
  position: Position;
  scope: Scope;
  appliedToIds: string[]
  minLimit: PriceLimit | null;
  maxLimit: PriceLimit | null;
  isActive: boolean;
}

export interface Position {
  value?: number;
  unit?: Unit;
  matchType: MatchType;
  reference: PositionReference;
}

export interface PriceLimit {
  limitType: LimitType;
  limitValue: number;
}

export enum LimitType {
 FIXED_AMOUNT = 'FIXED_AMOUNT'
}

export enum MatchType {
  EQUALS = 'EQUALS',
  HIGHER = 'HIGHER',
  LOWER = 'LOWER'
}

export enum PositionReference {
  HIGHEST = 'HIGHEST',
  LOWEST = 'LOWEST',
  AVERAGE = 'AVERAGE'
}

export enum Unit {
  EUR = 'EUR',
  PERCENTAGE = 'PERCENTAGE'
}

export enum Scope {
  PRODUCT = 'PRODUCT',
  CATEGORY = 'CATEGORY'
}

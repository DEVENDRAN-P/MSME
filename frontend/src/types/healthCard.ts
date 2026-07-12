export interface DimensionScores {
  revenue_health: number;
  compliance_health: number;
  liquidity_health: number;
  workforce_health: number;
}

export interface CreditHealthCard {
  unified_score: number;
  grade: 'PRIME_PLUS' | 'PRIME' | 'NEAR_PRIME' | 'SUB_PRIME';
  description: string;
  dimension_scores: DimensionScores;
}

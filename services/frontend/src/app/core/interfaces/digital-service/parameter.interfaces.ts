export interface DigitalServiceParameterIa {
    modelDetails: string;
    parameters: string;
    framework: string;
    quantization: string;
    inference: boolean;
    finetuning: boolean;
    numberOfUsersPerYear: number;
    averageRequestsPerUser: number;
    averageTokensPerRequest: number;
    totalTokenGenerate: number;
}

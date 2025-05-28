export interface DigitalServiceParameterIa {
    id?: number;
    creationDate?: string;
    lastUpdateDate?: string;
    modelName: string;
    nbParameters: string;
    framework: string;
    quantization: string;
    isInference: boolean;
    isFinetuning: boolean;
    numberUserYear: number;
    averageNumberToken: number;
    averageNumberRequest: number;
    totalGeneratedTokens: number;
}

export interface AiModelConfig {
    modelName: string;
    parameters: string;
    framework: string;
    quantization: string;
}

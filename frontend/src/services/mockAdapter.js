export function mockSuccess(data, delay = 120) {
  return async (config) => {
    await new Promise((resolve) => window.setTimeout(resolve, delay))

    return {
      data: {
        code: 0,
        message: 'ok',
        data,
      },
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    }
  }
}
